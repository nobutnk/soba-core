package soba.testdata.inheritance1;

import java.util.function.Consumer;

import soba.testdata.inheritance2.F;
import soba.testdata.inheritance2.H;

public class M extends C implements I, K {

    private Consumer<String> z = value -> {o(value);};
	
	public M() {
		this(K.x);
		System.err.println("D.<init>");
	}
	
	public M(int x) {
		super(x);
		System.err.println("D.<init>(int)");
	}
	
	public void m() {
		super.m();
		System.err.println("D.m");
	}
	
	public void testPackagePrivate() {
		C c = new C(0);
		c.n();
	}

	public void testPackagePrivate2() {
		C c = new F();
		c.n();
	}

	public void testPackagePrivate3() {
		C c = new G();
		c.n();
	}

	public void testPackagePrivate4() {
		C c = new H();
		c.n();
	}

	public void n() {
		System.err.println("D.n()");
	}
	
	public void o(String s) {
	    System.out.println("Hello, " + s);
	}
	
	public int example(int i, long l, double d, String s) {
		return i;
	}
	
	public void x(int t) {
		System.out.println("D.x(int)");
		z.accept("test");
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	public void p(Consumer<String> c) {
	    c.accept("test");
	}
	
	public void q() {
	    p(z);
	}
}
