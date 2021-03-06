package lxu.lxdfs.metadata;

import java.io.Serializable;

/**
 * DataNodeCommand.java
 * Created by magl on 14/11/19.
 *
 * The base class for all commands {@link lxu.lxdfs.namenode.NameNode} sent.
 */
public class DataNodeCommand implements Serializable {
    protected static final int DELETE_BLOCK = 0;
    protected static final int RESTORE_BLOCK = 1;
    protected int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
