package processes;

import java.io.Serializable;

/**
 * Created by Tong Wei on 8/28/14.
 */
public interface MigratableProcess extends Runnable, Serializable {

	public void suspend();
	// @override
	// toString() print the class name of the process as well
	// as the original set of arguments with which it was called
	//
}