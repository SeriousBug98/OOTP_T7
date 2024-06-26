
package dvm.domain.network;

public class Message {
    private MsgType msg_type;
    private String src_id;
    private String dst_id;
    private MsgContent msg_content;


    public Message(MsgType type, String srcId, String dstId, MsgContent msgContent) {
        this.msg_type = type;
        this.src_id = srcId;
        this.dst_id = dstId;
        this.msg_content = msgContent;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + msg_type +
                ", srcId='" + src_id + '\'' +
                ", dstId='" + dst_id + '\'' +
                ", content=" + msg_content +
                '}';
    }

    public MsgType getType() {
        return msg_type;
    }

    public String getSrcId() {
        return src_id;
    }

    public String getDstId() {
        return dst_id;
    }

    public void setDstId(String dstId) {
        this.dst_id = dstId;
    }

    public MsgContent getContent() {
        return msg_content;
    }

}
