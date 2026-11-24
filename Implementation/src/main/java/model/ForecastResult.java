package model;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates the results of a time-series forecasting model evaluation.
 * Stores model name, actual historical values, generated forecasts, and accuracy metrics (MAE, RMSE).
 */
public class ForecastResult implements Serializable {

    private String modelName;
    private List<String> periods;        // Labels (e.g., "2024-01", "2024-02")
    private List<Double> actualValues;
    private List<Double> forecastValues;
    private double mae;                  // Mean Absolute Error
    private double rmse;                 // Root Mean Squared Error
    private double nextMonthForecast;    // Forecast for the immediate next period
    private String description;

    public ForecastResult(String modelName, List<String> periods, List<Double> actualValues,
                          List<Double> forecastValues, double mae, double rmse,
                          double nextMonthForecast, String description) {
        this.modelName = modelName;
        this.periods = periods;
        this.actualValues = actualValues;
        this.forecastValues = forecastValues;
        this.mae = mae;
        this.rmse = rmse;
        this.nextMonthForecast = nextMonthForecast;
        this.description = description;
    }

    public String getModelName() {
        return modelName;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public List<Double> getActualValues() {
        return actualValues;
    }

    public List<Double> getForecastValues() {
        return forecastValues;
    }

    public double getMae() {
        return mae;
    }

    public double getRmse() {
        return rmse;
    }

    public double getNextMonthForecast() {
        return nextMonthForecast;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s (MAE=%.2f, RMSE=%.2f, Next=%.1f)", modelName, mae, rmse, nextMonthForecast);
    }
}
