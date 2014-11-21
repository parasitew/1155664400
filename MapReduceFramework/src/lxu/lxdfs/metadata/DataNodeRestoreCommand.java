package lxu.lxdfs.metadata;

/**
 * Created by Wei on 11/21/14.
 */
public class DataNodeRestoreCommand extends DataNodeCommand {
	private Block block;
	private DataNodeDescriptor dataNode;

	public DataNodeRestoreCommand(Block block, DataNodeDescriptor dataNode) {
		this.type = this.RESTORE_BLOCK;
		this.block = block;
		this.dataNode = dataNode;
	}
}
