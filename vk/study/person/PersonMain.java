package vk.study.person;

public class PersonMain {

	public static void main(String[] args) {
		Person person = new Person("john");
		System.out.println(person);
	}

	public static class Person {
		public final String name;

		public Person(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Person{" +
						"name='" + name + '\'' +
					'}';
		}
	}
}
