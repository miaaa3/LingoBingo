export class RecentActivityDTO {
    id?: number;
    userId?: number;
    activityType: string; // You can use an enum for activity types
    entityId: number;
    timestamp?: Date;

}