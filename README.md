# kafkaParallelIO
Java NIO java TransferTo方法优化：并行读取磁盘IO

**Benchmark**
> * Java Zero-Copy技术性能测试
> * Java 普通Socket IO性能测试
> * Java NIO性能测试以及并行性能测试
> * Kafka性能测试以及Kafak并行性能测试

**KafkaParallel Core**
> * Kafka并行传输直接内存池
> * 利用DirectMemoryChannel并行或者串行的读取数据
