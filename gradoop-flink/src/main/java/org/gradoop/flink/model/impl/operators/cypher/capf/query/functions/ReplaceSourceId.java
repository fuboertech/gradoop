/*
 * Copyright © 2014 - 2019 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.flink.model.impl.operators.cypher.capf.query.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.FunctionAnnotation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.Properties;

/**
 * Replace the source GradoopId of an edge tuple with the matching Long id;
 */

@FunctionAnnotation.ForwardedFieldsFirst("f0->f0;f2->f2;f3->f3")
@FunctionAnnotation.ForwardedFieldsSecond("f0->f1")
public class ReplaceSourceId implements JoinFunction<
  Tuple5<Long, GradoopId, GradoopId, String, Properties>,
  Tuple2<Long, Vertex>,
  Tuple5<Long, Long, GradoopId, String, Properties>> {

  /**
   * Reduce object instantiations
   */
  private Tuple5<Long, Long, GradoopId, String, Properties> returnTuple = new Tuple5<>();

  @Override
  public Tuple5<Long, Long, GradoopId, String, Properties> join(
    Tuple5<Long, GradoopId, GradoopId, String, Properties> inputTuple,
    Tuple2<Long, Vertex> vertexTuple
  ) throws Exception {
    returnTuple.f0 = inputTuple.f0;
    returnTuple.f1 = vertexTuple.f0;
    returnTuple.f2 = inputTuple.f2;
    returnTuple.f3 = inputTuple.f3;
    returnTuple.f4 = inputTuple.f4;

    return returnTuple;
  }
}
