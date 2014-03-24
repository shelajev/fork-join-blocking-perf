package org.sample;

import java.util.concurrent.RecursiveTask;

public class Cos extends RecursiveTask<Double> implements DummyComputableThing{

  private double input;

  public Cos(double input) {
    this.input = input;
  }

  @Override
  protected Double compute() {
    return Math.cos(input);
  }

  @Override
  public double dummyCompute() {
    return compute();
  }
}
