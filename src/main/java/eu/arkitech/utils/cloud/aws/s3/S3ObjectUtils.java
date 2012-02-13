package eu.arkitech.utils.cloud.aws.s3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.model.S3Object;

public abstract class S3ObjectUtils
{
	public static String getS3ObjectAsString(S3Object s3obj) throws IOException
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(s3obj.getObjectContent()));
		final StringBuffer sb = new StringBuffer();
		while (true)
		{
			String line = reader.readLine();
			if (line == null)
				break;
			sb.append(line);
		}
		return sb.toString();
	}
}
