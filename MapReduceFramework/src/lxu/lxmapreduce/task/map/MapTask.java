package lxu.lxmapreduce.task.map;

import lxu.lxdfs.metadata.LocatedBlock;
import lxu.lxmapreduce.io.RecordReader;
import lxu.lxmapreduce.io.RecordWriter;
import lxu.lxmapreduce.io.format.InputFormat;
import lxu.lxmapreduce.io.format.OutputFormat;
import lxu.lxmapreduce.task.Task;
import lxu.lxmapreduce.task.TaskAttemptID;
import lxu.lxmapreduce.tmp.Configuration;
import lxu.lxmapreduce.tmp.JobConf;
import lxu.lxmapreduce.tmp.TaskAttemptContext;
import lxu.utils.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Wei on 11/12/14.
 */
public class MapTask extends Task {
	private List<String> inputsplits;

	public MapTask(TaskAttemptID attemptID, int partition, LocatedBlock locatedBlock) {
		super(attemptID, partition, locatedBlock);

		// TODO: Init inputsplits
		inputsplits.add(locatedBlock.getBlock().getBlockID() + ".blk");
	}

	public static void main(String[] args) {

	}

	@Override
	public void run(JobConf jobConf) throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		// start thread that will handle communication with parent
//		TaskReporter reporter = new TaskReporter();
//		reporter.startCommunicationThread();
//
		this.runMapper(jobConf);
//		done();
	}

	private <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
	void runMapper(JobConf jobConf) throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException,
			InstantiationException, ClassNotFoundException {

		// Create taskContext.
		TaskAttemptContext taskContext = new TaskAttemptContext(jobConf, this.getTaskAttemptID());

		// Create InputFormat (Lin).
		InputFormat inputFormat = (InputFormat)
				ReflectionUtils.newInstance(taskContext.getInputFormatClass());

		OutputFormat outputFormat = (OutputFormat)
				ReflectionUtils.newInstance(taskContext.getOutputFormatClass());

		// Create mapper
		Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> mapper = (Mapper<KEYIN, VALUEIN, KEYOUT,
				VALUEOUT>) ReflectionUtils.newInstance(jobConf.getMapperClass());
		/* TODO Init form input block files. */
		// Create LineRecordReader.
		RecordReader input = inputFormat.createRecordReader();
		RecordWriter output = outputFormat.createRecordWriter();

		// Create mapperContext
		Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context mapperContext = null;
		Constructor<Mapper.Context> contextConstructor = Mapper.Context.class.getConstructor
				(new Class[]{Mapper.class,
						Configuration.class,
						RecordReader.class,
						RecordWriter.class});

		// Set input file and output file.
		input.initialize(this.inputsplits);
		output.initialize(this.inputsplits);

		mapperContext = contextConstructor.newInstance(mapper, jobConf, input, output);

		mapper.run(mapperContext);

		input.close();
		output.close();
	}
}