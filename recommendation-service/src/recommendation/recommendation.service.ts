import { Injectable, Logger } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Recommendation, RecommendationDocument } from './schemas/recommendation.schema';
import { UserPreferences, UserPreferencesDocument } from './schemas/user-preferences.schema';
import { FoodAnalytics, FoodAnalyticsDocument } from './schemas/food-analytics.schema';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class RecommendationService {
  private readonly logger = new Logger(RecommendationService.name);
  private readonly restaurantServiceUrl: string;

  constructor(
    @InjectModel(Recommendation.name) private recommendationModel: Model<RecommendationDocument>,
    @InjectModel(UserPreferences.name) private preferencesModel: Model<UserPreferencesDocument>,
    @InjectModel(FoodAnalytics.name) private analyticsModel: Model<FoodAnalyticsDocument>,
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.restaurantServiceUrl = configService.get('RESTAURANT_SERVICE_URL', 'http://localhost:8082');
  }

  // Feign Scenario 4 (NestJS): Get menu info from Restaurant Service via HTTP
  async getRestaurantMenu(restaurantId: string): Promise<any[]> {
    try {
      const response = await firstValueFrom(
        this.httpService.get(`${this.restaurantServiceUrl}/api/v1/restaurants/${restaurantId}/menu`),
      );
      this.logger.log(`Fetched menu for restaurant ${restaurantId}`);
      return response.data;
    } catch (error: any) {
      this.logger.error(`Failed to fetch menu for restaurant ${restaurantId}`, error.message);
      return [];
    }
  }

  async getRecommendationsForUser(userId: string): Promise<Recommendation[]> {
    const prefs = await this.preferencesModel.findOne({ userId });
    const analytics = await this.analyticsModel
      .find({}).sort({ orderCount: -1 }).limit(10);

    const recommendations = await this.recommendationModel
      .find({ userId }).sort({ score: -1 }).limit(10);

    if (recommendations.length === 0) {
      // Generate basic recommendations based on popular items
      const popularRestaurants = [...new Set(analytics.map(a => a.restaurantId))].slice(0, 5);
      return popularRestaurants.map(restaurantId => ({
        userId,
        restaurantId,
        score: Math.random() * 100,
        reasons: ['Popular in your area', 'Top rated'],
        metadata: {},
      } as Recommendation));
    }

    return recommendations;
  }

  async updateUserPreferences(userId: string, preferences: Partial<UserPreferences>): Promise<UserPreferences> {
    return this.preferencesModel.findOneAndUpdate(
      { userId },
      { $set: preferences },
      { upsert: true, new: true },
    );
  }

  async getUserPreferences(userId: string): Promise<UserPreferences | null> {
    return this.preferencesModel.findOne({ userId });
  }

  async trackOrderEvent(event: {
    userId: string; restaurantId: string; menuItemId: string; rating?: number;
  }): Promise<void> {
    // Update user preferences
    await this.preferencesModel.findOneAndUpdate(
      { userId: event.userId },
      {
        $addToSet: {
          orderedRestaurants: event.restaurantId,
          orderedItems: event.menuItemId,
        },
      },
      { upsert: true },
    );

    // Update analytics
    await this.analyticsModel.findOneAndUpdate(
      { restaurantId: event.restaurantId, menuItemId: event.menuItemId },
      {
        $inc: { orderCount: 1 },
        ...(event.rating && {
          $set: { averageRating: event.rating },
        }),
      },
      { upsert: true },
    );

    this.logger.log(`Tracked order event for user ${event.userId}`);
  }

  async getFoodAnalytics(restaurantId: string): Promise<FoodAnalytics[]> {
    return this.analyticsModel.find({ restaurantId }).sort({ orderCount: -1 });
  }
}


