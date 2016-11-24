package pl.parser.nbp.service;

import pl.parser.nbp.domain.ExchangeRate;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collector;

/**
 * Provides methods for calculating certain statistics about given exchange rates.
 * Currently provides methods for calculating the mean of buying exchange rates,
 * as well as for calculating standard deviation of selling exchange rates.
 *
 * @author Grzegorz Soliński
 */
public class ExchangeRateCalculator {

    /**
     * Calculates the mean of buying exchange rates from the given list of ExchangeRate objects.
     *
     * @param exchangeRates list of ExchangeRate objects, for which the mean will be computed
     *
     * @return average buying exchange rate of the given ExchangeRate objects
     */
    public static double avgBuyingExchangeRate(List<ExchangeRate> exchangeRates) {
        return exchangeRates.stream()
                .mapToDouble(ExchangeRate::getBuyingRate)
                .average().orElse(0.0);                     // if the Optional<Double>  is empty
                                                            // (e.g. when list is empty), return 0.0
    }

    /**
     * Calculates the standard deviaiton of selling exchange rates from the given list
     * of ExchangeRate objects.
     *
     * @param exchangeRates list of ExchangeRate objects, for which the standard deviation will be
     *                      computed
     *
     * @return standard deviation of all selling exchange rates among the given ExchangeRate objects
     */
    public static double stdDevSellingExchangeRate(List<ExchangeRate> exchangeRates) {
        return exchangeRates.stream()
                .map(ExchangeRate::getSellingRate)
                .collect(DoubleStatistics.collector())
                .getStandardDeviation();
    }

    // class is an extension to DoubleSummaryStatistics to enable calculation of standard deviation
    private static class DoubleStatistics extends DoubleSummaryStatistics {

        private double sumOfSquare = 0.0d;
        private double sumOfSquareCompensation; // Low order bits of sum
        private double simpleSumOfSquare; // Used to compute right sum for non-finite inputs

        @Override
        public void accept(double value) {
            super.accept(value);
            double squareValue = value * value;
            simpleSumOfSquare += squareValue;
            sumOfSquareWithCompensation(squareValue);
        }

        public DoubleStatistics combine(DoubleStatistics other) {
            super.combine(other);
            simpleSumOfSquare += other.simpleSumOfSquare;
            sumOfSquareWithCompensation(other.sumOfSquare);
            sumOfSquareWithCompensation(other.sumOfSquareCompensation);
            return this;
        }

        private void sumOfSquareWithCompensation(double value) {
            double tmp = value - sumOfSquareCompensation;
            double comp = sumOfSquare + tmp;
            sumOfSquareCompensation = (comp - sumOfSquare) - tmp;
            sumOfSquare = comp;
        }

        public double getSumOfSquare() {
            double tmp =  sumOfSquare + sumOfSquareCompensation;
            if (Double.isNaN(tmp) && Double.isInfinite(simpleSumOfSquare)) {
                return simpleSumOfSquare;
            }
            return tmp;
        }

        public final double getStandardDeviation() {
            return getCount() > 0 ? Math.sqrt((getSumOfSquare() / getCount()) -
                    Math.pow(getAverage(), 2)) : 0.0d;
        }

        public static Collector<Double, ?, DoubleStatistics> collector() {
            return Collector.of(DoubleStatistics::new, DoubleStatistics::accept,
                    DoubleStatistics::combine);
        }
    }
}
