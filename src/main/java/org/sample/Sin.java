package org.sample;

import java.util.concurrent.RecursiveTask;

public class Sin extends RecursiveTask<Double> implements DummyComputableThing {

  private double input;

  public Sin(double input) {
    this.input = input;
  }

  @Override
  protected Double compute() {
    return Math.sin(input);
  }

  @Override
  public double dummyCompute() {
    return compute();
  }
}
