package one.keia.oss.psql.protocol.types

import kotlin.jvm.JvmInline

public data class Vec2(val x: Double, val y: Double)

@JvmInline
public value class Point(public val value: Vec2) {
    public companion object : DataType<Point>(600, Codec.todo())

    public data object Array : DataType.Array<Point>(1017, Point)
}

@JvmInline
public value class LineSegment(public val value: Pair<Vec2, Vec2>) {
    public companion object : DataType<LineSegment>(601, Codec.todo())

    public data object Array : DataType.Array<LineSegment>(1018, LineSegment)
}

@JvmInline
public value class Path(public val value: List<Vec2>) {
    public companion object : DataType<Path>(602, Codec.todo())

    public data object Array : DataType.Array<Path>(1019, Path)
}


public data class Circle(val coords: Vec2, val radius: Float) {
    public companion object : DataType<Circle>(718, Codec.todo())

    public data object Array : DataType.Array<Circle>(719, Circle)

}

public data class Box(val value: Pair<Vec2, Vec2>) {
    public companion object : DataType<Box>(603, Codec.todo())

    public data object Array : DataType.Array<Box>(1020, Box)
}

@JvmInline
public value class Polygon(public val points: List<Vec2>) {
    public companion object : DataType<Polygon>(604, Codec.todo())

    public data object Array : DataType.Array<Polygon>(1027, Polygon)
}

public data class Line(val a: Double, val b: Double, val c: Double) {
    public companion object : DataType<Line>(628, Codec.todo())

    public data object Array : DataType.Array<Line>(629, Line)
}
