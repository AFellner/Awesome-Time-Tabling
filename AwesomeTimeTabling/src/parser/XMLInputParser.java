package parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import model.Model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import structure.Course;
import structure.Curriculum;
import structure.Room;
import structure.Teacher;



/**
 * This class represents an instance of parser. The parser takes an input of
 * CB-CTT input file in XML format, and then parse it into objects defined in
 * the structure package.
 * 
 * @author Radityo
 * @version 1.0
 * 
 */
public class XMLInputParser
{

	/**
	 * This method takes an CB-CTT XML file containing a timetabling problem as
	 * input, and then parse it into objects defined in the structure package.
	 * 
	 * @param xmlInputFile
	 *            string object containing the path to the desired input file.
	 * @return a model representing the the given timetabling problem.
	 */
	public static Model ParseXML(String xmlInputFile)
	{

		final Model model = new Model(xmlInputFile);

		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				String		currentParentPosition	= "_";
				String		currentParentAttribute	= "_";
				Curriculum	curriculum;
				int			courseCounter			= 0;
				int			roomCounter				= 0;
				int			curriculaCounter		= 0;

				boolean parseYesOrNo(String yesOrNo)
				{
					if (yesOrNo.equalsIgnoreCase("yes"))
						return true;
					else if (yesOrNo.equalsIgnoreCase("no"))
						return false;
					else
						return false;
				}

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException
				{

					// System.out.println("Start Element :" + qName);

					if (qName.equalsIgnoreCase("courses"))
					{
						currentParentPosition = "courses";
					}
					else if (qName.equalsIgnoreCase("rooms"))
					{
						currentParentPosition = "rooms";
					}
					else if (qName.equalsIgnoreCase("curriculum"))
					{
						currentParentPosition = "curriculum";
						curriculum = new Curriculum(attributes.getValue(0));
						model.getIdMapper().put(attributes.getValue(0),
								curriculaCounter);
						curriculaCounter++;
					}
					else if (qName.equalsIgnoreCase("constraint"))
					{
						currentParentPosition = "constraint";
						currentParentAttribute = attributes.getValue(1);
					}

					if (qName.equalsIgnoreCase("days"))
					{
						model.setDays(Integer.parseInt(attributes.getValue(0)));
					}

					else if (qName.equalsIgnoreCase("periods_per_day"))
					{
						model.setPeriodsPerDay(Integer.parseInt(attributes
								.getValue(0)));
					}

					else if (qName.equalsIgnoreCase("daily_lectures"))
					{
						model.setMinDailyLectures(Integer.parseInt(attributes
								.getValue(0)));

						model.setMaxDailyLectures(Integer.parseInt(attributes
								.getValue(1)));
					}

					else if (qName.equalsIgnoreCase("course")
							&& currentParentPosition.equals("courses"))
					{
						Course course = new Course(
						// id (name)
								attributes.getValue(0),
								// studentcapacity
								Integer.parseInt(attributes.getValue(4)),
								// minworkdays
								Integer.parseInt(attributes.getValue(3)),
								// doublelecture
								parseYesOrNo(attributes.getValue(5)),
								// teacher
								attributes.getValue(1),
								// lecturecount
								Integer.parseInt(attributes.getValue(2)));

						if (!model.getIdMapper().containsKey(
								attributes.getValue(1)))
						{
							Teacher t = new Teacher(attributes.getValue(1));
							model.getModelTeacher().add(t);
							model.getIdMapper().put(attributes.getValue(1),
									model.getModelTeacher().size() - 1);
						}

						model.getModelTeacher()
								.get(model.getIdMapper().get(
										attributes.getValue(1)))
								.addCourse(course);

						model.getModelCourses().add(course);
						model.getIdMapper().put(attributes.getValue(0),
								courseCounter);
						courseCounter++;
					}

					else if (qName.equalsIgnoreCase("room")
							&& currentParentPosition.equals("rooms"))
					{
						Room room = new Room(attributes.getValue(0),
								Integer.parseInt(attributes.getValue(1)));

						model.getModelRooms().add(room);

						model.getIdMapper().put(attributes.getValue(0),
								roomCounter);
						roomCounter++;
					}

					else if (qName.equalsIgnoreCase("course")
							&& currentParentPosition.equals("curriculum"))
					{
						curriculum.addCourse(model.getModelCourses()
								.get(model.getIdMapper().get(
										attributes.getValue(0))));
					}

					else if (qName.equalsIgnoreCase("timeslot")
							&& currentParentPosition.equals("constraint"))
					{
						int period = Integer.parseInt(attributes.getValue(0))
								* model.getDayDifference()
								+ Integer.parseInt(attributes.getValue(1));
						model.getModelCourses()
								.get(model.getIdMapper().get(
										currentParentAttribute))
								.addNotAllowedPeriod(period);
					}

					else if (qName.equalsIgnoreCase("room")
							&& currentParentPosition.equals("constraint"))
					{
						model.getModelCourses()
								.get(model.getIdMapper().get(
										currentParentAttribute))
								.addNotAllowedRoom(
										model.getModelRooms()
												.get(model.getIdMapper().get(
														attributes.getValue(0))));
					}

				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException
				{

					if (qName.equalsIgnoreCase("courses")
							|| qName.equalsIgnoreCase("rooms")
							|| qName.equalsIgnoreCase("curriculum")
							|| qName.equalsIgnoreCase("constraint"))
					{
						currentParentPosition = "_";
					}

					if (qName.equals("curriculum"))
					{
						model.getModelCurricula().add(curriculum);
					}

				}

				public void characters(char ch[], int start, int length)
						throws SAXException
				{

				}

			};

			saxParser.parse(xmlInputFile, handler);

		}
		catch (ParserConfigurationException e)
		{
			System.err.println("Error in XML File!");
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.err.println("Cannot access the given file!");
			e.printStackTrace();
		}
		
		model.setModelLectures();
		return model;
	}
}
