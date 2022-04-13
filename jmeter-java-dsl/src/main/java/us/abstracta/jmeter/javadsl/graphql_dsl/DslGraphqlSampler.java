package us.abstracta.jmeter.javadsl.graphql_dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.apache.jmeter.protocol.http.util.GraphQLRequestParamUtils;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.util.HashMap;

public class DslGraphqlSampler extends DslHttpSampler {

    private final GraphQLRequestParams params = new GraphQLRequestParams();
    private final HashMap<String, Object> variables = new HashMap<>();

    public DslGraphqlSampler(String name, String url) {
        super(name, url);
        this.contentType(ContentType.APPLICATION_JSON);
    }

    public DslGraphqlSampler(String url) {
        super(null, url);
        this.contentType(ContentType.APPLICATION_JSON);
    }

    private void updateBody() {
        this.body(GraphQLRequestParamUtils.toPostBodyString(params));
    }

    public DslGraphqlSampler query(String query) {
        params.setQuery(query);
        this.updateBody();
        return this;
    }

    private String variablesToJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(variables);
    }

    public <T> DslGraphqlSampler variable(String name, T value) throws JsonProcessingException {
        variables.put(name, value);
        params.setVariables(variablesToJsonString());
        this.updateBody();
        return this;
    }

    @Override
    public DslGraphqlSampler method(String method) {
        super.method(method);
        return this;
    }

    public static DslGraphqlSampler graphqlSampler(String url) {
        return new DslGraphqlSampler(url);
    }
}
