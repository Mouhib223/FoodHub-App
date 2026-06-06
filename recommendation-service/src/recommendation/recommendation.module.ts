import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { HttpModule } from '@nestjs/axios';
import { RecommendationController } from './recommendation.controller';
import { RecommendationService } from './recommendation.service';
import { Recommendation, RecommendationSchema } from './schemas/recommendation.schema';
import { UserPreferences, UserPreferencesSchema } from './schemas/user-preferences.schema';
import { FoodAnalytics, FoodAnalyticsSchema } from './schemas/food-analytics.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Recommendation.name, schema: RecommendationSchema },
      { name: UserPreferences.name, schema: UserPreferencesSchema },
      { name: FoodAnalytics.name, schema: FoodAnalyticsSchema },
    ]),
    HttpModule,
  ],
  controllers: [RecommendationController],
  providers: [RecommendationService],
})
export class RecommendationModule {}

