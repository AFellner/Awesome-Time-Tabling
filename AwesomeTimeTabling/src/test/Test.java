package test;

import parser.XMLInputGenerator;
import parser.XMLInputParser;
import structure.*;
import model.*;



public class Test
{

	public static void main(String[] args)
	{
		//XMLInputGenerator.generateXMLInputFile("randominput2.xml", 5, 6, 2, 3,
		//		10, 7, 2, 6, 2, 4, 20, 60, 5, 2, 5, 3, 6, 5, 4, 8, 4, 1, 3);
		Model model = XMLInputParser.ParseXML("comp03.xml");
		// System.out.println(Lecture.getLectureNumberCounter());
		// System.out.println(Room.getRoomNumberCounter());
		//System.out.println(model.toString());

		// System.out.println(model.roomOccupancy());

		// System.out.println(model.doubleLectures());

		// Model test = new Model("bla");

		// Lecture bla = new Lecture(null,"bla");

		// System.out.println(test.lectAtDay(bla, 1));

		// int[] set = {1,2,5,7,4};
		// processSubsets(set,2);

		// System.out.println(model.roomSuitability());
//		Model model = XMLInputParser.ParseXML("comp03.xml");
		//System.out.println(Lecture.getLectureNumberCounter());
		//System.out.println(Room.getRoomNumberCounter());
		//System.out.println(model.toString());
		
		//System.out.println(model.roomOccupancy());
		
		//System.out.println(model.doubleLectures());
		
		//Model test = new Model("bla");
		
		//Lecture bla = new Lecture(null,"bla");
		
		//System.out.println(test.lectAtDay(bla, 1));
		
		//int[] set = {1,2,5,7,4};
		//processSubsets(set,2);
		model.compute();

	}
}
