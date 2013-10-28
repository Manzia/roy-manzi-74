package com.manzia.shopping.reviews;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * The MzReviewsMergeJob creates a file in the SequenceFile format with a
 * Text key and Text Value. <br>
 * The Key is the category::productSKU string while the Value will be the
 * concatenation of all review comments associated with this specific product SKU.<br>
 * <p>A Mapper reads each line of input: category::productSKU:::review comments. It outputs
 * the category::productSKU as the Key and the review comments as Value. A Reducer receives
 * all the review comments from a particular productSKU, joins them into one string, and
 * outputs this as the Value, with the category::productSKU as the Key. The data is written
 * into a SequenceFile that can be read by a dictionary Vectorizer to generate the TF vectors
 * that are the input to the CVB-LDA algorithm.</p>
 * 
 * @author Roy Manzi Tumubweinee, Jan 16, 2013, Manzia Corporation
 *
 */
public class MzReviewsMergeJob extends Configured implements Tool {
	
	//Logger
	public static final Logger logger = 
					Logger.getLogger(MzReviewsMergeJob.class.getCanonicalName());
	
	// Mapper
	public static class ByKeyMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		private final Pattern keySplitter = Pattern.compile(":::");
		private final int reviewField = 1; 	// review
		private final int categorySkuField = 0;	// category::productSKU field
		
		@Override
		protected void map(LongWritable key, Text value, Context context) throws 
		InterruptedException, IOException {
			
			// Split line using Regex
			String[] fields = keySplitter.split(value.toString());
			assert keySplitter != null;
			if (fields.length -1 < reviewField || fields.length -1 < categorySkuField) {
				
				// Count line errors
				context.getCounter("Map", "LinesWithErrors").increment(1);
				return;
			}
			
			// Ouput the key-Value pair
			String reviewKey = fields[categorySkuField];
			assert reviewKey != null;
			String reviewValue = fields[reviewField];
			assert reviewValue != null;
			context.write(new Text(reviewKey), new Text(reviewValue));
		}
	}
	
	// Reducer
	public static class ByKeyReducer extends Reducer<Text, Text, Text, Text> {
		
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) 
				throws InterruptedException, IOException {
			StringBuffer mergeReview = new StringBuffer();
			for (Text value : values) {
				
				// Concatenate reviews
				mergeReview.append(value.toString()).append(" ");
			}
			
			// output merged Review
			context.write(key, new Text(mergeReview.toString().trim()));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		
		// Configure the Job
		Configuration conf = getConf();
		Job job = new Job(conf);
		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		job.setJobName("MzReviewsMergeJob");
		job.setMapperClass(ByKeyMapper.class);
		job.setReducerClass(ByKeyReducer.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		// Run the Job
		//job.setNumReduceTasks(0);
		job.setJarByClass(MzReviewsMergeJob.class);
		if (!job.waitForCompletion(true)) {
		      throw new IllegalStateException("MzReviewsMergeJob failed processing ");
		}
		return 0;
	}

	/**
	 * @param args - 2 directory paths /inputPathDir /outputPathDir
	 * @throws Exception - thrown if Job fails
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: MzReviewsMergeJob <inputPath> <outputPath>");			
			System.exit(-1);
		}
		if (!new File(args[0]).isFile()) {
			System.err.println("Invalid input File specified!");			
			System.exit(-1);
		}
		int res = ToolRunner.run(new Configuration(), new MzReviewsMergeJob(), args);
		System.exit(res);

	}

}
