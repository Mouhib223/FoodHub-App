import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

@Schema({ timestamps: true })
export class UserPreferences {
  @Prop({ required: true, unique: true })
  userId: string;

  @Prop({ type: [String], default: [] })
  favoriteCuisines: string[];

  @Prop({ type: [String], default: [] })
  dietaryRestrictions: string[];

  @Prop({ default: 0 })
  maxDeliveryFee: number;

  @Prop({ type: [String], default: [] })
  orderedRestaurants: string[];

  @Prop({ type: [String], default: [] })
  orderedItems: string[];
}

export const UserPreferencesSchema = SchemaFactory.createForClass(UserPreferences);
export type UserPreferencesDocument = UserPreferences & Document;

