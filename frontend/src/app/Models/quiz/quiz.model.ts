import { Question } from "./question.model";

export class Quiz {
    id!: number;
    name!: string;
    description!: string;
    createdAt?: Date;
    questions!: Question[];
    isPublic?: boolean; // Indicates if the set is public or private

}