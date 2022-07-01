### Benchmarking - Why

- Baseline key algorithms/components
- Fail early
- Reproduce issues (> stress, > get (un)lucky)
- Qualify
- Complement Code Reviews
- Educate

### Benchmarking is hard,really hard

- Multi-threading
- Mistakes/limits/knowledge - JMM,measure time,stats
- JIT & CPU (inlining,constant folding,loop unrolling,false sharing,...)
- Should you write your own framework?

### Use JMH

- Java Micro benchmark Harness(JMH)
- Openjdk tool(JIT crowd)
- Write-Run-Profile-Report benchmarks
  - Write
    - Some guidelines & Api's to know about
      - Use BlackHole and/or return
      - Use State and Scope
    - @Setup @Benchmark @TearDown
  - Run
    - Warmup needed
    - #threads,#iterations
    - Multiple modes
      - Throughput
      - Avg Time
      - Sample Time(precentiles)
      - Single Shot
- Abstracts away the hard part,@-based



[参考](https://www.youtube.com/watch?v=Bi0E7w1ZFFA)