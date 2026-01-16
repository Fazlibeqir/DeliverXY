/**
 * Centralized error handling middleware
 */

import { alert } from "@nativescript/core";
import { logger } from "./logger";

export interface AppError {
  message: string;
  code?: string;
  statusCode?: number;
  originalError?: any;
}

export class ErrorHandler {
  /**
   * Handle API errors and convert to user-friendly messages
   */
  static handleApiError(error: any): AppError {
    let message = "An unexpected error occurred";
    let code: string | undefined;
    let statusCode: number | undefined;

    if (error instanceof Error) {
      message = error.message;
      
      // Handle specific error types
      if (message === "UNAUTHORIZED") {
        message = "Your session has expired. Please log in again.";
        code = "UNAUTHORIZED";
      } else if (message.includes("Network")) {
        message = "Network error. Please check your internet connection.";
        code = "NETWORK_ERROR";
      } else if (message.includes("timeout")) {
        message = "Request timed out. Please try again.";
        code = "TIMEOUT";
      }
    } else if (typeof error === "string") {
      message = error;
    } else if (error?.response) {
      // HTTP error response
      statusCode = error.response.status;
      const data = error.response.data;
      
      if (data?.message) {
        message = data.message;
      } else if (data?.error) {
        message = typeof data.error === 'string' ? data.error : data.error.message || message;
      } else {
        message = `Request failed with status ${statusCode}`;
      }
      
      code = `HTTP_${statusCode}`;
    } else if (error?.message) {
      message = error.message;
    }

    const appError: AppError = {
      message,
      code,
      statusCode,
      originalError: error
    };

    logger.error("API Error:", appError);
    return appError;
  }

  /**
   * Show user-friendly error alert
   */
  static showError(error: AppError | string | Error, title: string = "Error") {
    const message = typeof error === "string" 
      ? error 
      : error instanceof Error 
        ? error.message 
        : error.message;
    
    alert({
      title,
      message,
      okButtonText: "OK"
    });
  }

  /**
   * Handle and show error in one call
   */
  static handleAndShow(error: any, title: string = "Error") {
    const appError = this.handleApiError(error);
    this.showError(appError, title);
    return appError;
  }

  /**
   * Check if error is a network error
   */
  static isNetworkError(error: any): boolean {
    return error?.code === "NETWORK_ERROR" || 
           error?.message?.toLowerCase().includes("network") ||
           error?.message?.toLowerCase().includes("connection");
  }

  /**
   * Check if error is an authentication error
   */
  static isAuthError(error: any): boolean {
    return error?.code === "UNAUTHORIZED" || 
           error?.statusCode === 401 ||
           error?.message === "UNAUTHORIZED";
  }
}
