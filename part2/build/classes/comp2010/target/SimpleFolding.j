; Jasmin Java assembler code that assembles the SimpleFolding example class

.source Type1.j
.class public SimpleFolding
.super java/lang/Object

.method public <init>()V
	aload_0
	invokenonvirtual java/lang/Object/<init>()V
	return
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 3

	getstatic java/lang/System/out Ljava/io/PrintStream;
	ldc 42
	ldc 12345
    iadd
    invokevirtual java/io/PrintStream/println(I)V
	return
.end method