package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import model.Model;



public class YicesOutputParser
{

	public static void ParseYicesOutput(Model model, String filename)
	{
		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new FileReader(new File(filename)));
			StringTokenizer tokenizer;
			String temp1 = "";
			String temp2 = "";

			temp1 = reader.readLine();
			System.out.println(temp1);
			if (!temp1.equalsIgnoreCase("sat"))
			{
				if (!temp1.equalsIgnoreCase("unknown"))
					model.setSatisfiability(false);
				reader.close();
				return;
			}

			model.setSatisfiability(true);
			temp1 = reader.readLine();
			while (!temp1.startsWith("(= ("))
			{
				tokenizer = new StringTokenizer(temp1, " ");
				temp1 = tokenizer.nextToken();
				temp1 = tokenizer.nextToken();
				temp2 = tokenizer.nextToken();
				temp2 = temp2.substring(0, temp2.length() - 1);
//				System.out.println(temp1);
				model.getModelLectures().get(model.getIdMapper().get(temp1))
						.setTimeSlot(Integer.parseInt(temp2));
				temp1 = reader.readLine();
			}
			while (temp1.startsWith("(= ("))
			{
				tokenizer = new StringTokenizer(temp1, " ");
				temp1 = tokenizer.nextToken();
				temp1 = tokenizer.nextToken();
				temp1 = tokenizer.nextToken();
				temp2 = tokenizer.nextToken();

				temp1 = temp1.substring(0, temp1.length() - 1);
				temp2 = temp2.substring(0, temp2.length() - 1);

				model.getModelLectures()
						.get(Integer.parseInt(temp1))
						.setRoom(
								model.getModelRooms().get(
										Integer.parseInt(temp2)));
				temp1 = reader.readLine();
			}
			temp1 = reader.readLine();
			temp1 = temp1.substring(6, temp1.length());
			model.setCost(Integer.parseInt(temp1));

			reader.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
