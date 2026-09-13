import { Game } from "./Game";

export interface Player {
    id: number;
    games: Game [];
    username: string;
    avatarUrl: string;
    score: number;
finished: boolean;}