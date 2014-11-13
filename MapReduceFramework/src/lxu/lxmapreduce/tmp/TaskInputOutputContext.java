package lxu.lxmapreduce.tmp;

import lxu.lxmapreduce.io.RecordWriter;

import java.io.IOException;

/**
 * Created by Wei on 11/12/14.
 */
public abstract class TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
		extends TaskAttemptContext {

	private RecordWriter<KEYOUT, VALUEOUT> out;

	public TaskInputOutputContext(Configuration conf,
	                              TaskID taskId,
	                              RecordWriter<KEYOUT, VALUEOUT> out) {
		super(conf, taskId);
		this.out = out;
	}

	public abstract boolean nextKeyValue() throws IOException;

	public abstract KEYIN getCurrentKey();

	public abstract VALUEIN getCurrentValue();

	public void write(KEYOUT key, VALUEOUT value) throws IOException {
		out.write(key, value);
	}
}
