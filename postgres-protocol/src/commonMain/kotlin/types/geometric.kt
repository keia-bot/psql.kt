package one.keia.oss.psql.protocol.types

import kotlin.jvm.JvmInline

public data class Vec2(val x: Float, val y: Float)

@JvmInline
public value class Point(public val value: Vec2) {
    public companion object : DataType<Point>(600), Codec<Point> by Codec.None()
}

@JvmInline
public value class Lseg(public val value: Pair<Vec2, Vec2>) {
    public companion object : DataType<Lseg>(601), Codec<Lseg> by Codec.None()
}

@JvmInline
public value class Path(public val value: List<Vec2>) {
    public companion object : DataType<Path>(602), Codec<Path> by Codec.None()
}
