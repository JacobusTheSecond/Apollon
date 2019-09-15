package apollon.dynamics.data.theta;

import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

public class Column implements ThetaFunction {
    private final ThetaFunction function;

    private final String id;

    public Column(@NotNull String id, @NotNull ThetaFunction function) {
        this.id = id;
        this.function = function;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public double[] compute(@NotNull Row row) {
        return function.compute(row);
    }

    @Override
    public String toString() {
        return id;
    }

    @NotNull
    public static Column one() {
        return new Column("1", ThetaFunction.ONE);
    }

    @NotNull
    public static Column variable(@NotNull Variables variables, int index) {
        return new Column(variables.get(index), row -> row.get(index));
    }

    @NotNull
    public static Column signum(@NotNull Variables variables, int index) {
        return variableFunction(variables, index, id -> "sgn(" + id + ")", Math::signum);
    }

    @NotNull
    public static Column signum(@NotNull String column) {
        return unaryFunction(column, id -> "sgn(" + id + ")", Math::signum);
    }

    @NotNull
    public static Column absolute(@NotNull Variables variables, int index) {
        return variableFunction(variables, index, id -> "abs(" + id + ")", Math::abs);
    }

    @NotNull
    public static Column absolute(@NotNull String column) {
        return unaryFunction(column, id -> "abs(" + id + ")", Math::abs);
    }

    @NotNull
    public static Column sin(@NotNull Variables variables, double coefficient, int index) {
        return variableFunction(variables, index, id -> "sin(" + coefficient + " * " + id + ")", value -> Math.sin(coefficient * value));
    }

    @NotNull
    public static Column sin(double coefficient, @NotNull String column) {
        return unaryFunction(column, id -> "sin(" + coefficient + " * " + id + ")", value -> Math.sin(coefficient * value));
    }

    @NotNull
    public static Column cos(@NotNull Variables variables, double coefficient, int index) {
        return variableFunction(variables, index, id -> "cos(" + coefficient + " * " + id + ")", value -> Math.cos(coefficient * value));
    }

    @NotNull
    public static Column cos(double coefficient, @NotNull String column) {
        return unaryFunction(column, id -> "cos(" + coefficient + " * " + id + ")", value -> Math.cos(coefficient * value));
    }

    @NotNull
    public static Column multiply(@NotNull String leftColumn, @NotNull String rightColumn) {
        return binaryFunction(leftColumn, rightColumn, (left, right) -> left + " * " + right, (left, right) -> left * right);
    }

    @NotNull
    public static Column power(@NotNull Variables variables, int index, int exponent) {
        return variableFunction(variables, index, id -> exponent == 0 ? "1" : exponent == 1 ? id : id + "^" + exponent, value -> Math.pow(value, exponent));
    }

    @NotNull
    public static Column power(@NotNull String column, int exponent) {
        return unaryFunction(column, id -> exponent == 0 ? "1" : exponent == 1 ? id : id + "^" + exponent, value -> Math.pow(value, exponent));
    }

    @NotNull
    public static Column variableFunction(@NotNull Variables variables, int index, @NotNull UnaryOperator<String> id, @NotNull DoubleUnaryOperator function) {
        return new Column(id.apply(variables.get(index)), ThetaFunction.variableFunction(index, function));
    }

    @NotNull
    public static Column unaryFunction(@NotNull String column, @NotNull UnaryOperator<String> id, @NotNull DoubleUnaryOperator function) {
        return new Column(id.apply(column), ThetaFunction.unaryFunction(column, function));
    }

    @NotNull
    public static Column variableFunction(@NotNull Variables variables, int leftIndex, int rightIndex, @NotNull BinaryOperator<String> id, @NotNull DoubleBinaryOperator function) {
        return new Column(id.apply(variables.get(leftIndex), variables.get(rightIndex)), ThetaFunction.variableFunction(leftIndex, rightIndex, function));
    }

    @NotNull
    public static Column binaryFunction(@NotNull Variables variables, int index, @NotNull String column, @NotNull BinaryOperator<String> id,
                                        @NotNull DoubleBinaryOperator function) {
        return new Column(id.apply(variables.get(index), column), ThetaFunction.binaryFunction(index, column, function));
    }

    @NotNull
    public static Column binaryFunction(@NotNull String leftColumn, @NotNull String rightColumn, @NotNull BinaryOperator<String> id, @NotNull DoubleBinaryOperator function) {
        return new Column(id.apply(leftColumn, rightColumn), ThetaFunction.binaryFunction(leftColumn, rightColumn, function));
    }
}
