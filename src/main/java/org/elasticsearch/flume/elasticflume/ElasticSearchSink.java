package org.elasticsearch.flume.elasticflume;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class ElasticSearchSink extends EventSink.Base {
    private Node node;
    private Client client;
    private String indexName = "flume";
    private static final String LOG_TYPE = "LOG";
    private Charset charset = Charset.defaultCharset();

    @Override
    public void append(Event e) throws IOException {

        IndexResponse response = client.prepareIndex(indexName, LOG_TYPE, null)
                .setSource(jsonBuilder()
                        .startObject()
                        .field("message", new String(e.getBody(), charset))
                        .field("timestamp", new Date(e.getTimestamp()))
                        .field("host", e.getHost())
                        .field("priority", e.getPriority().name())
                        // TODO add attributes
                        .endObject()
                )
                .execute()
                .actionGet();
    }


    @Override
    public void close() throws IOException {
        super.close();

        client.close();
        node.close();
    }

    @Override
    public void open() throws IOException {
        super.open();

        node = nodeBuilder().client(true).node();
        client = node.client();

    }


    public static SinkFactory.SinkBuilder builder() {

        return new SinkFactory.SinkBuilder() {
            @Override
            public EventSink build(Context context, String... argv) {
                // TODO fill in cluster details etc. 
                return new ElasticSearchSink();

            }
        };
    }

}
