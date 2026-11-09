package com.lingobingo.Service;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/** Pure unit tests for the bingo engine — no Spring context, no database. */
class BingoServiceTest {

    private final BingoService bingo = new BingoService(new Random(42));

    // ---------------------------------------------------------------- uniqueness

    @RepeatedTest(20)
    void gridsNeverShareTheSameSetOfWords() {
        BingoService svc = new BingoService(); // fresh randomness each repetition
        var r = svc.generate(30, 5, BingoService.MAX_GRIDS, true);
        assertEquals("words", r.uniqueBy());
        assertEquals(BingoService.MAX_GRIDS, r.grids().size());
        assertAllSetsDistinct(r.grids());
    }

    @Test
    void noWordAppearsTwiceInOneGrid() {
        for (int[] g : bingo.generate(20, 4, 100, false).grids()) {
            long distinct = Arrays.stream(g).distinct().count();
            assertEquals(g.length, distinct, "duplicate square in " + Arrays.toString(g));
        }
    }

    @Test
    void everyWordIndexIsInRange() {
        for (int[] g : bingo.generate(12, 3, 50, true).grids())
            for (int w : g) assertTrue(w == BingoService.FREE || (w >= 0 && w < 12));
    }

    @Test
    void requestingExactlyTheMaximumNumberOfSetsTerminatesAndIsComplete() {
        // C(10,9) = 10 possible word sets for a 3x3 grid: ask for all 10.
        var r = bingo.generate(10, 3, 10, false);
        assertEquals("words", r.uniqueBy());
        assertAllSetsDistinct(r.grids());
    }

    @Test
    void nearTheLimitUsesEnumerationAndStillReturnsDistinctSets() {
        // C(11,9) = 55; asking for 55 would make random rejection crawl.
        var r = bingo.generate(11, 3, 55, false);
        assertEquals(55, r.grids().size());
        assertAllSetsDistinct(r.grids());
    }

    @Test
    void fallsBackToDistinctLayoutsWhenSetsCannotBeUnique() {
        // 9 words, 3x3, no free square: only one possible set of words.
        var r = bingo.generate(9, 3, 30, false);
        assertEquals("layout", r.uniqueBy());
        Set<String> layouts = new HashSet<>();
        for (int[] g : r.grids()) assertTrue(layouts.add(Arrays.toString(g)), "duplicate layout");
    }

    @Test
    void freeCentreIsInTheMiddleOnlyForOddSizes() {
        int[] g5 = bingo.generate(30, 5, 1, true).grids().get(0);
        assertEquals(BingoService.FREE, g5[12]);
        assertEquals(1, Arrays.stream(g5).filter(w -> w == BingoService.FREE).count());

        int[] g4 = bingo.generate(30, 4, 1, true).grids().get(0);
        assertTrue(Arrays.stream(g4).noneMatch(w -> w == BingoService.FREE), "4x4 has no centre square");
    }

    // ---------------------------------------------------------------- edge cases

    @Test
    void tooFewWordsIsAClearError() {
        var e = assertThrows(IllegalArgumentException.class, () -> bingo.generate(8, 3, 1, false));
        assertTrue(e.getMessage().contains("needs 9"));
    }

    @Test
    void freeCentreLowersTheWordsNeeded() {
        assertDoesNotThrow(() -> bingo.generate(8, 3, 1, true));
    }

    @Test
    void outOfBoundsRequestsAreRejected() {
        // Even the smallest grid (3x3 + free = 8 squares) has 8! = 40320 layouts,
        // so with the MAX_GRIDS cap a batch is always possible once sizes are valid.
        assertThrows(IllegalArgumentException.class, () -> bingo.generate(30, 5, BingoService.MAX_GRIDS + 1, false));
        assertThrows(IllegalArgumentException.class, () -> bingo.generate(30, 2, 1, false));
        assertThrows(IllegalArgumentException.class, () -> bingo.generate(60, 6, 1, false));
        assertThrows(IllegalArgumentException.class, () -> bingo.generate(30, 4, 0, false));
    }

    @Test
    void binomialIsExact() {
        assertEquals(10, BingoService.binomial(10, 9).intValue());
        assertEquals(55, BingoService.binomial(11, 9).intValue());
        assertEquals("593775", BingoService.binomial(30, 24).toString());
        assertEquals(0, BingoService.binomial(5, 6).intValue());
    }

    // ---------------------------------------------------------------- win detection

    private static int[] grid4() { return IntStream.range(0, 16).toArray(); } // word i at square i

    @Test
    void nothingCalledNoWin() {
        assertFalse(bingo.check(grid4(), List.of()).won());
    }

    @Test
    void detectsEachRowColumnAndBothDiagonals() {
        assertEquals(List.of("row 2"), bingo.check(grid4(), List.of(4, 5, 6, 7)).lines());
        assertEquals(List.of("column 3"), bingo.check(grid4(), List.of(2, 6, 10, 14)).lines());
        assertEquals(List.of("diagonal \\"), bingo.check(grid4(), List.of(0, 5, 10, 15)).lines());
        assertEquals(List.of("diagonal /"), bingo.check(grid4(), List.of(3, 6, 9, 12)).lines());
    }

    @Test
    void almostALineIsNotAWin() {
        assertFalse(bingo.check(grid4(), List.of(4, 5, 6)).won());
        // three corners + scattered squares: no line
        assertFalse(bingo.check(grid4(), List.of(0, 3, 12, 5, 9)).won());
    }

    @Test
    void wordsThatAreNotOnTheGridAreIgnored() {
        assertFalse(bingo.check(grid4(), List.of(99, 100, 101, 102)).won());
    }

    @Test
    void freeCentreCountsAsMarked() {
        int[] g = {0, 1, 2, 3, BingoService.FREE, 5, 6, 7, 8};
        assertEquals(List.of("row 2"), bingo.check(g, List.of(3, 5)).lines());
        assertEquals(List.of("diagonal \\"), bingo.check(g, List.of(0, 8)).lines());
    }

    @Test
    void reportsSeveralLinesAtOnce() {
        var r = bingo.check(grid4(), List.of(0, 1, 2, 3, 4, 8, 12));
        assertEquals(List.of("row 1", "column 1"), r.lines());
    }

    @Test
    void fullCardWinsEverything() {
        var r = bingo.check(grid4(), IntStream.range(0, 16).boxed().toList());
        assertEquals(10, r.lines().size()); // 4 rows + 4 columns + 2 diagonals
    }

    @Test
    void rejectsNonSquareGrids() {
        assertThrows(IllegalArgumentException.class, () -> bingo.check(new int[10], List.of()));
    }

    @Test
    void generatedGridsWinOnlyWhenTheirWordsAreCalled() {
        // end-to-end: call exactly the first row of a generated grid
        int[] g = bingo.generate(25, 5, 1, true).grids().get(0);
        List<Integer> firstRow = Arrays.stream(g, 0, 5).boxed().toList();
        assertEquals(List.of("row 1"), bingo.check(g, firstRow).lines());
    }

    // ---------------------------------------------------------------- helpers

    private static void assertAllSetsDistinct(List<int[]> grids) {
        Set<String> keys = new HashSet<>();
        for (int[] g : grids) {
            int[] words = Arrays.stream(g).filter(w -> w != BingoService.FREE).toArray();
            assertTrue(keys.add(BingoService.setKey(words)), "two grids share the same words: " + Arrays.toString(g));
        }
    }
}
