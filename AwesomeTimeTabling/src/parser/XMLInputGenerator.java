package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;



public class XMLInputGenerator
{

	public static void generateXMLInputFile(String filename, int days,
			int periods_per_day, int daily_lectures_min,
			int daily_lectures_max, int courses, int teachers,
			int lectures_count_min, int lectures_count_max, int min_day_min,
			int min_day_max, int student_cap_min, int student_cap_max,
			int rooms, int buildings, int curricula,
			int min_course_in_curriculum, int max_course_in_curriculum,
			int period_constraints, int min_periods_in_constraint,
			int max_periods_in_constraint, int room_constraints,
			int min_rooms_in_constraint, int max_rooms_in_constraint)
	{

		try
		{
			ArrayList<Integer> periodsSet = new ArrayList<Integer>();
			ArrayList<String> teachersSet = new ArrayList<String>();
			ArrayList<String> coursesSet = new ArrayList<String>();
			ArrayList<String> curriculaSet = new ArrayList<String>();
			ArrayList<String> roomsSet = new ArrayList<String>();
			ArrayList<Integer> buildingSet = new ArrayList<Integer>();
			ArrayList<Integer> lecturesCountSet = new ArrayList<Integer>();
			ArrayList<Integer> minDaySet = new ArrayList<Integer>();
			ArrayList<Integer> studentCapSet = new ArrayList<Integer>();
			ArrayList<Integer> curriculumCourseSet = new ArrayList<Integer>();
			ArrayList<Integer> periodConstraintSet = new ArrayList<Integer>();
			ArrayList<Integer> roomConstraintSet = new ArrayList<Integer>();
			ArrayList<Boolean> yesOrNo = new ArrayList<Boolean>();
			yesOrNo.add(true);
			yesOrNo.add(false);

			for (int i = 0; i < days; i++)
			{
				for (int j = 0; j < periods_per_day; j++)
				{
					periodsSet.add(i * 100 + j);
				}
			}
			for (int i = 0; i < teachers; i++)
			{
				teachersSet.add(("t" + i));
			}
			for (int i = 0; i < courses; i++)
			{
				coursesSet.add(("c" + i));
			}
			for (int i = 0; i < curricula; i++)
			{
				curriculaSet.add(("cur" + i));
			}
			for (int i = 0; i < rooms; i++)
			{
				roomsSet.add(("r" + i));
			}
			for (int i = 0; i < buildings; i++)
			{
				buildingSet.add((i));
			}
			for (int i = lectures_count_min; i <= lectures_count_max; i++)
			{
				lecturesCountSet.add(i);
			}
			for (int i = min_day_min; i <= min_day_max; i++)
			{
				minDaySet.add(i);
			}
			for (int i = student_cap_min; i <= student_cap_max; i++)
			{
				studentCapSet.add(i);
			}
			for (int i = min_course_in_curriculum; i <= max_course_in_curriculum; i++)
			{
				curriculumCourseSet.add(i);
			}
			for (int i = min_periods_in_constraint; i <= max_periods_in_constraint; i++)
			{
				periodConstraintSet.add(i);
			}
			for (int i = min_rooms_in_constraint; i <= max_rooms_in_constraint; i++)
			{
				roomConstraintSet.add(i);
			}

			StringBuilder x = new StringBuilder();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					filename)));

			x.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			x.append("<instance name=\"" + filename + "\">\n");
			x.append("\t<descriptor>\n");
			x.append("\t\t<days value=\"" + days + "\"/>\n");
			x.append("\t\t<periods_per_day value=\"" + periods_per_day
					+ "\"/>\n");
			x.append("\t\t<daily_lectures min=\"" + daily_lectures_min
					+ "\" max=\"" + daily_lectures_max + "\"/>\n");
			x.append("\t</descriptor>\n");
			x.append("\t<courses>\n");
			for (int i = 0; i < courses; i++)
			{
				x.append("\t\t<course id=\"" + coursesSet.get(i)
						+ "\" teacher=\"" + getRandom(teachersSet)
						+ "\" lectures=\"" + getRandom(lecturesCountSet)
						+ "\" min_days=\"" + getRandom(minDaySet)
						+ "\" students=\"" + getRandom(studentCapSet)
						+ "\" double_lectures=\"" + getRandom(yesOrNo)
						+ "\"/>\n");
			}
			x.append("\t</courses>\n");
			x.append("\t<rooms>\n");
			for (int i = 0; i < rooms; i++)
			{
				x.append("\t\t<room id=\"" + roomsSet.get(i) + "\" size=\""
						+ getRandom(studentCapSet) + "\" building=\""
						+ getRandom(buildingSet) + "\"/>\n");
			}
			x.append("\t</rooms>\n");
			x.append("\t<curricula>\n");
			for (int i = 0; i < curricula; i++)
			{
				x.append("\t\t<curriculum id=\"" + curriculaSet.get(i)
						+ "\">\n");
				int k = getRandom(curriculumCourseSet);
				ArrayList<String> temp = new ArrayList<String>(coursesSet);
				for (int j = 0; j < k; j++)
				{
					String lalala = getRandom(temp);
					x.append("\t\t\t<course ref=\"" + lalala + "\"/>\n");
					temp.remove(lalala);
				}
				x.append("\t\t</curriculum>\n");
			}
			x.append("\t</curricula>\n");
			x.append("\t<constraints>\n");
			ArrayList<String> temp = new ArrayList<String>(coursesSet);
			for (int i = 0; i < period_constraints; i++)
			{
				ArrayList<Integer> temp1 = new ArrayList<Integer>(periodsSet);
				String chosenCourse = getRandom(temp);
				x.append("\t\t<constraint type=\"period\" course=\""
						+ chosenCourse + "\">\n");
				int k = getRandom(periodConstraintSet);
				for (int j = 0; j < k; j++)
				{
					Integer chosenTimeslot = getRandom(temp1);
					x.append("\t\t\t<timeslot day=\"" + chosenTimeslot / 100
							+ "\" period=\"" + chosenTimeslot % 100 + "\"/>\n");
					temp1.remove(chosenTimeslot);
				}
				temp.remove(chosenCourse);
				x.append("\t\t</constraint>\n");

			}
			temp = new ArrayList<String>(coursesSet);
			for (int i = 0; i < room_constraints; i++)
			{
				ArrayList<String> temp1 = new ArrayList<String>(roomsSet);
				String chosenCourse = getRandom(temp);
				x.append("\t\t<constraint type=\"room\" course=\""
						+ chosenCourse + "\">\n");
				int k = getRandom(roomConstraintSet);
				for (int j = 0; j < k; j++)
				{
					String chosenRoom = getRandom(temp1);
					x.append("\t\t\t<room ref=\"" + chosenRoom + "\"/>\n");
					temp1.remove(chosenRoom);
				}
				temp.remove(chosenCourse);
				x.append("\t\t</constraint>\n");

			}
			x.append("\t</constraints>\n");
			x.append("</instance>\n");
			writer.write(x.toString());
			
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static <T extends Object> T getRandom(ArrayList<T> set)
	{
		Collections.shuffle(set);
		return set.get(0);
	}

}
