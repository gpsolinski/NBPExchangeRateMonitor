package pl.parser.nbp.service.impl;

import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.service.ExchangeRateCalculator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collector;

/**
 * Created by Grzegorz on 23.11.2016.
 */
public class ExchangeRateCalculatorImpl implements ExchangeRateCalculator {


    @Override
    public double avgBuyingExchangeRate(List<ExchangeRate> exchangeRateTables) {
        return exchangeRateTables.stream()
                .mapToDouble(ExchangeRate::getBuyingRate)
                .average().orElse(0.0);
    }

    @Override
    public double stdDevSellingExchangeRate(List<ExchangeRate> exchangeRateTables) {
        return exchangeRateTables.stream()
                .map(ExchangeRate::getSellingRate)
                .collect(DoubleStatistics.collector())
                .getStandardDeviation();
    }

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
            double velvel = sumOfSquare + tmp; // Little wolf of rounding error
            sumOfSquareCompensation = (velvel - sumOfSquare) - tmp;
            sumOfSquare = velvel;
        }

        public double getSumOfSquare() {
            double tmp =  sumOfSquare + sumOfSquareCompensation;
            if (Double.isNaN(tmp) && Double.isInfinite(simpleSumOfSquare)) {
                return simpleSumOfSquare;
            }
            return tmp;
        }

        public final double getStandardDeviation() {
            return getCount() > 0 ? Math.sqrt((getSumOfSquare() / getCount()) - Math.pow(getAverage(), 2)) : 0.0d;
        }

        public static Collector<Double, ?, DoubleStatistics> collector() {
            return Collector.of(DoubleStatistics::new, DoubleStatistics::accept, DoubleStatistics::combine);
        }
    }
}
