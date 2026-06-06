import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

export type RecommendationDocument = Recommendation & Document;

@Schema({ timestamps: true })
export class Recommendation {
  @Prop({ required: true })
  userId: string;

  @Prop({ required: true })
  restaurantId: string;

  @Prop({ required: true })
  score: number;

  @Prop({ type: [String], default: [] })
  reasons: string[];

  @Prop({ type: Object })
  metadata: Record<string, any>;
}

export const RecommendationSchema = SchemaFactory.createForClass(Recommendation);

