package apollon.dynamics.data.theta;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@FunctionalInterface
public interface ThetaFunction {
    @NotNull double[] compute(@NotNull Row row);

    ThetaFunction ONE = row -> {
        double[] value = new double[row.size()];
        Arrays.fill(value, 1);
        return value;
    };

    @NotNull
    static ThetaFunction variableFunction(int index, @NotNull DoubleUnaryOperator function) {
        return row -> vector(function).apply(row.get(index));
    }

    @NotNull
    static ThetaFunction unaryFunction(@NotNull String column, @NotNull DoubleUnaryOperator function) {
        return row -> vector(function).apply(row.get(column));
    }

    @NotNull
    static ThetaFunction variableFunction(int leftIndex, int rightIndex, @NotNull DoubleBinaryOperator function) {
        return row -> vector(function).apply(row.get(leftIndex), row.get(rightIndex));
    }

    @NotNull
    static ThetaFunction binaryFunction(int index, @NotNull String column, @NotNull DoubleBinaryOperator function) {
        return row -> vector(function).apply(row.get(index), row.get(column));
    }

    @NotNull
    static ThetaFunction binaryFunction(@NotNull String leftColumn, @NotNull String rightColumn, @NotNull DoubleBinaryOperator function) {
        return row -> vector(function).apply(row.get(leftColumn), row.get(rightColumn));
    }

    @NotNull
    static UnaryOperator<double[]> vector(@NotNull DoubleUnaryOperator function) {
        return values -> DoubleStream.of(values).map(function).toArray();
    }

    @NotNull
    static BinaryOperator<double[]> vector(@NotNull DoubleBinaryOperator function) {
        return (left, right) -> IntStream.range(0, left.length).mapToDouble(i -> function.applyAsDouble(left[i], right[i])).toArray();
    }
}
