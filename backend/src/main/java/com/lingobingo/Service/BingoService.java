package com.lingobingo.Service;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

/**
 * Classroom vocabulary bingo: builds a batch of grids that are all different
 * and decides, reliably, whether a grid has won.
 *
 * <p>Pure logic, no database, so it is fully unit-tested (BingoServiceTest).
 * Words are referred to by index (0..n-1) in the teacher's word list; the
 * controller maps them back to flashcard terms.
 *
 * <h2>"All different" — what it means here</h2>
 * Two grids are different when they do not contain the same <i>set</i> of
 * words. That is stronger than "different layout": two grids with the same
 * words merely shuffled would always fill up on the same calls, which feels
 * unfair in class. Only when the word list is too small to give every student
 * a distinct set (e.g. 9 words for a 3x3 grid) do we fall back to distinct
 * layouts, and the result says so ({@code uniqueBy = "layout"}).
 *
 * <h2>Generation strategy</h2>
 * <ul>
 *   <li>Many possible word sets compared to grids requested (the usual case):
 *       random sampling (partial Fisher-Yates) + a HashSet of canonical keys
 *       rejects the rare duplicate. Expected retries stay tiny.</li>
 *   <li>Few possible sets (request close to the maximum): random rejection
 *       would spin (coupon-collector problem), so we enumerate every
 *       combination once, shuffle, and take the first N — guaranteed to end.</li>
 * </ul>
 */
@Service
public class BingoService {

    /** Above this many combinations we sample; below it we enumerate. */
    static final long ENUMERATION_LIMIT = 20_000;
    /** Hard cap so a single request cannot ask for an absurd batch. */
    public static final int MAX_GRIDS = 200;

    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 5;

    /** Marker for the free centre square in a grid. */
    public static final int FREE = -1;

    public record GenerationResult(List<int[]> grids, String uniqueBy) {}

    public record WinResult(boolean won, List<String> lines) {}

    private final Random random;

    public BingoService() { this(new Random()); }

    BingoService(Random random) { this.random = random; }

    // ------------------------------------------------------------------ generation

    /**
     * @param wordCount number of distinct words available
     * @param size      grid side (3..5)
     * @param count     how many grids to produce (1..MAX_GRIDS)
     * @param freeCenter put a FREE square in the middle (odd sizes only)
     * @return grids as flat row-major arrays of word indexes (FREE for the centre)
     * @throws IllegalArgumentException when the request cannot be satisfied
     */
    public GenerationResult generate(int wordCount, int size, int count, boolean freeCenter) {
        if (size < MIN_SIZE || size > MAX_SIZE)
            throw new IllegalArgumentException("Grid size must be between " + MIN_SIZE + " and " + MAX_SIZE + ".");
        if (count < 1 || count > MAX_GRIDS)
            throw new IllegalArgumentException("Number of grids must be between 1 and " + MAX_GRIDS + ".");

        boolean free = freeCenter && size % 2 == 1;
        int cells = size * size - (free ? 1 : 0);
        if (wordCount < cells)
            throw new IllegalArgumentException("This set has " + wordCount + " words but a " + size + "x" + size
                    + " grid needs " + cells + ". Add words or choose a smaller grid.");

        BigInteger combos = binomial(wordCount, cells);
        List<int[]> picks;
        String uniqueBy;

        if (combos.compareTo(BigInteger.valueOf(count)) >= 0) {
            uniqueBy = "words";
            picks = combos.compareTo(BigInteger.valueOf(ENUMERATION_LIMIT)) <= 0
                    ? enumeratePick(wordCount, cells, count)
                    : samplePick(wordCount, cells, count);
            // the chosen words are then laid out in random order on the grid
            for (int[] p : picks) shuffle(p);
        } else {
            // Not enough words for distinct sets: distinct layouts instead.
            BigInteger layouts = combos.multiply(factorial(cells));
            if (layouts.compareTo(BigInteger.valueOf(count)) < 0)
                throw new IllegalArgumentException("Only " + layouts + " different grids are possible with "
                        + wordCount + " words. Add words or ask for fewer grids.");
            uniqueBy = "layout";
            picks = sampleLayouts(wordCount, cells, count);
        }

        List<int[]> grids = new ArrayList<>(picks.size());
        for (int[] p : picks) grids.add(free ? insertFree(p, size) : p);
        return new GenerationResult(grids, uniqueBy);
    }

    /** Random k-subsets, rejecting duplicates by their sorted key. */
    private List<int[]> samplePick(int n, int k, int count) {
        Set<String> seen = new HashSet<>();
        List<int[]> out = new ArrayList<>(count);
        int[] pool = range(n);
        while (out.size() < count) {
            // partial Fisher-Yates: first k slots become a uniform random subset
            for (int i = 0; i < k; i++) {
                int j = i + random.nextInt(n - i);
                int t = pool[i]; pool[i] = pool[j]; pool[j] = t;
            }
            int[] pick = Arrays.copyOf(pool, k);
            if (seen.add(setKey(pick))) out.add(pick);
        }
        return out;
    }

    /** Every k-subset (lexicographic), shuffled, first `count` kept. */
    private List<int[]> enumeratePick(int n, int k, int count) {
        List<int[]> all = new ArrayList<>();
        int[] c = range(k);
        while (true) {
            all.add(c.clone());
            int i = k - 1;
            while (i >= 0 && c[i] == n - k + i) i--;
            if (i < 0) break;
            c[i]++;
            for (int j = i + 1; j < k; j++) c[j] = c[j - 1] + 1;
        }
        Collections.shuffle(all, random);
        return new ArrayList<>(all.subList(0, count));
    }

    /** Random ordered picks, rejecting identical layouts. Only used when sets can't be unique. */
    private List<int[]> sampleLayouts(int n, int k, int count) {
        Set<String> seen = new HashSet<>();
        List<int[]> out = new ArrayList<>(count);
        int[] pool = range(n);
        int guard = 0;
        while (out.size() < count) {
            if (++guard > count * 1000)
                throw new IllegalStateException("Could not build enough different grids; add more words.");
            shuffle(pool);
            int[] pick = Arrays.copyOf(pool, k);
            if (seen.add(Arrays.toString(pick))) out.add(pick);
        }
        return out;
    }

    private int[] insertFree(int[] words, int size) {
        int[] g = new int[size * size];
        int mid = g.length / 2, w = 0;
        for (int i = 0; i < g.length; i++) g[i] = (i == mid) ? FREE : words[w++];
        return g;
    }

    // ------------------------------------------------------------------ win detection

    /**
     * Checks a grid against the words the teacher has actually called. A
     * student's own ticks are never trusted: a square counts only if its word
     * was called (or it is the FREE centre).
     *
     * @return every completed line: "row 1".."row N", "column 1"..,
     *         "diagonal \\" and "diagonal /"
     */
    public WinResult check(int[] grid, Collection<Integer> called) {
        int n = (int) Math.round(Math.sqrt(grid.length));
        if (n * n != grid.length || n < MIN_SIZE || n > MAX_SIZE)
            throw new IllegalArgumentException("A grid must be square, between 3x3 and 5x5.");

        Set<Integer> calledSet = new HashSet<>(called);
        boolean[] hit = new boolean[grid.length];
        for (int i = 0; i < grid.length; i++)
            hit[i] = grid[i] == FREE || calledSet.contains(grid[i]);

        List<String> lines = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            boolean ok = true;
            for (int c = 0; c < n && ok; c++) ok = hit[r * n + c];
            if (ok) lines.add("row " + (r + 1));
        }
        for (int c = 0; c < n; c++) {
            boolean ok = true;
            for (int r = 0; r < n && ok; r++) ok = hit[r * n + c];
            if (ok) lines.add("column " + (c + 1));
        }
        boolean d1 = true, d2 = true;
        for (int i = 0; i < n; i++) {
            d1 &= hit[i * n + i];
            d2 &= hit[i * n + (n - 1 - i)];
        }
        if (d1) lines.add("diagonal \\");
        if (d2) lines.add("diagonal /");
        return new WinResult(!lines.isEmpty(), lines);
    }

    // ------------------------------------------------------------------ helpers

    static String setKey(int[] pick) {
        int[] s = pick.clone();
        Arrays.sort(s);
        return Arrays.toString(s);
    }

    static BigInteger binomial(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        k = Math.min(k, n - k);
        BigInteger r = BigInteger.ONE;
        for (int i = 0; i < k; i++)
            r = r.multiply(BigInteger.valueOf(n - i)).divide(BigInteger.valueOf(i + 1));
        return r;
    }

    private static BigInteger factorial(int k) {
        BigInteger r = BigInteger.ONE;
        for (int i = 2; i <= k; i++) r = r.multiply(BigInteger.valueOf(i));
        return r;
    }

    private static int[] range(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }

    private void shuffle(int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }
}
