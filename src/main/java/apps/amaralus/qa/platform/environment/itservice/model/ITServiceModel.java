package apps.amaralus.qa.platform.environment.itservice.model;

import apps.amaralus.qa.platform.project.linked.ProjectLinkedModel;
import apps.amaralus.qa.platform.rocksdb.sequence.GeneratedSequence;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

@Data
@NoArgsConstructor
@KeySpace("it-service")
public class ITServiceModel implements ProjectLinkedModel<Long> {
        @Id
        @GeneratedSequence("it-service-id")
        private Long id;
        private String name;
        private String description;
        private String project;
    private String host;
    private Integer port;
}
