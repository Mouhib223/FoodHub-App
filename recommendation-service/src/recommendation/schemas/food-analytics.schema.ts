import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

@Schema({ timestamps: true })
export class FoodAnalytics {
  @Prop({ required: true })
  restaurantId: string;

  @Prop({ required: true })
  menuItemId: string;

  @Prop({ default: 0 })
  orderCount: number;

  @Prop({ default: 0 })
  viewCount: number;

  @Prop({ default: 0 })
  averageRating: number;

  @Prop({ type: Object })
  popularHours: Record<string, number>;
}

export const FoodAnalyticsSchema = SchemaFactory.createForClass(FoodAnalytics);
export type FoodAnalyticsDocument = FoodAnalytics & Document;

