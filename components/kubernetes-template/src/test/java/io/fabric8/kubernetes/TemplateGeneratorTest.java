/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes;

import io.fabric8.common.util.IOHelpers;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Port;
import io.fabric8.kubernetes.api.model.ReplicationControllerSchema;
import io.fabric8.kubernetes.template.CreateAppDTO;
import io.fabric8.kubernetes.template.TemplateGenerator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.fabric8.common.util.Files.recursiveDelete;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 */
public class TemplateGeneratorTest {
    @Test
    public void testGenerateJson() throws Exception {
        String basedir = System.getProperty("basedir", ".");
        File jsonFile = new File(basedir + "/target/templateGenerator/sample.json").getCanonicalFile();
        recursiveDelete(jsonFile);


        CreateAppDTO dto = new CreateAppDTO();
        dto.setName("MyApp");
        dto.setDockerImage("fabric8/hawtio");
        dto.setReplicaCount(3);

        List<Port> ports = new ArrayList<>();
        Port jolokiaPort = new Port();
        jolokiaPort.setHostPort(10001);
        jolokiaPort.setContainerPort(8778);
        ports.add(jolokiaPort);
        dto.setPorts(ports);

        Map<String, String> labels = new HashMap<>();
        labels.put("foo", "bar");
        labels.put("drink", "beer");
        dto.setLabels(labels);

        TemplateGenerator generator = new TemplateGenerator(dto);
        generator.generate(jsonFile);

        String json = IOHelpers.readFully(jsonFile);
        System.out.println("Generated: " + json);

        Object loadedDTO = KubernetesHelper.loadJson(json);
        System.out.println("Loaded json DTO: " + loadedDTO);
        assertTrue("Loaded DTO should be a ReplicationControllerSchema but was " + loadedDTO, loadedDTO instanceof ReplicationControllerSchema);
        ReplicationControllerSchema replicationController = (ReplicationControllerSchema) loadedDTO;
    }

}