import { Controller, Get, Put, Post, Body, Param } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { RecommendationService } from './recommendation.service';

@ApiTags('Recommendations')
@Controller('api/v1/recommendations')
export class RecommendationController {

  constructor(private readonly recommendationService: RecommendationService) {}

  @Get('users/:userId')
  @ApiOperation({ summary: 'Get personalized recommendations for a user' })
  async getRecommendations(@Param('userId') userId: string) {
    return this.recommendationService.getRecommendationsForUser(userId);
  }

  @Get('users/:userId/preferences')
  @ApiOperation({ summary: 'Get user preferences' })
  async getPreferences(@Param('userId') userId: string) {
    return this.recommendationService.getUserPreferences(userId);
  }

  @Put('users/:userId/preferences')
  @ApiOperation({ summary: 'Update user preferences' })
  async updatePreferences(@Param('userId') userId: string, @Body() preferences: any) {
    return this.recommendationService.updateUserPreferences(userId, preferences);
  }

  @Get('restaurants/:restaurantId/analytics')
  @ApiOperation({ summary: 'Get food analytics for restaurant' })
  async getAnalytics(@Param('restaurantId') restaurantId: string) {
    return this.recommendationService.getFoodAnalytics(restaurantId);
  }

  // Feign Scenario 4: Get restaurant menu via HTTP (NestJS -> Spring Boot)
  @Get('restaurants/:restaurantId/menu')
  @ApiOperation({ summary: 'Get restaurant menu (calls Restaurant Service)' })
  async getRestaurantMenu(@Param('restaurantId') restaurantId: string) {
    return this.recommendationService.getRestaurantMenu(restaurantId);
  }

  @Post('events/order')
  @ApiOperation({ summary: 'Track order event for analytics' })
  async trackOrder(@Body() event: any) {
    await this.recommendationService.trackOrderEvent(event);
    return { message: 'Event tracked' };
  }
}

