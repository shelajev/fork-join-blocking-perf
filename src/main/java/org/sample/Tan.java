package org.sample;

import java.util.concurrent.RecursiveTask;

public class Tan extends RecursiveTask<Double> implements DummyComputableThing{

  private double input;

  public Tan(double input) {
    this.input = input;
  }

  @Override
  protected Double compute() {
    Sin sin = new Sin(input);
    Cos cos = new Cos(input);

    sin.fork();
    Double c = cos.invoke();
    Double s = sin.join();

    return s / c;
  }


  @Override
  public double dummyCompute() {
    Sin sin = new Sin(input);
    Cos cos = new Cos(input);

    return sin.dummyCompute() / cos.dummyCompute();
  }
}
