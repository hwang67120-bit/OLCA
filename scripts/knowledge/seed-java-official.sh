#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

post_knowledge() {
  local topic="$1"
  local keywords="$2"
  local content="$3"

  curl -sS -X POST "${BASE_URL}/api/knowledge" \
    -H "Content-Type: application/json" \
    -d "{
      \"topic\": \"${topic}\",
      \"keywords\": ${keywords},
      \"content\": ${content}
    }"
  printf '\n'
}

post_knowledge \
  "Java Official - Classes and Objects" \
  '["java","official","class","classes","object","objects","클래스","객체","oracle"]' \
  '"출처: Oracle Java Tutorials - Classes. URL: https://docs.oracle.com/javase/tutorial/java/javaOO/classes.html\n\nJava에서 클래스는 객체를 만들기 위한 설계 단위다. 클래스는 상태를 표현하는 필드, 동작을 표현하는 메서드, 객체 생성 시 초기값을 정하는 생성자를 함께 가진다. 객체는 클래스를 바탕으로 만들어진 실제 인스턴스이며, 각 객체는 자신의 필드 값을 가진다. 이 개념은 도메인 모델, DTO, 엔티티, 서비스 객체를 구분할 때 기본이 된다."'

post_knowledge \
  "Java Official - Interfaces" \
  '["java","official","interface","interfaces","api","contract","인터페이스","계약","oracle"]' \
  '"출처: Oracle Java Tutorials - Interfaces. URL: https://docs.oracle.com/javase/tutorial/java/IandI/createinterface.html\n\nJava에서 인터페이스는 구현 방식보다 외부에 제공할 동작의 약속을 먼저 정의하는 타입이다. 서로 다른 구현체가 같은 메서드 계약을 따르게 만들 수 있고, 사용하는 쪽은 구체 클래스보다 인터페이스에 의존할 수 있다. 인터페이스는 직접 인스턴스화할 수 없고 클래스가 구현하거나 다른 인터페이스가 확장할 수 있다. 이 개념은 API 설계, 의존성 역전, 다형성, 테스트 가능한 구조를 이해할 때 중요하다."'

post_knowledge \
  "Java Official - Inheritance" \
  '["java","official","inheritance","extends","superclass","subclass","상속","부모클래스","자식클래스","oracle"]' \
  '"출처: Oracle Java Tutorials - Inheritance. URL: https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html\n\nJava에서 상속은 기존 클래스의 필드와 메서드를 기반으로 새로운 클래스를 만드는 구조다. 자식 클래스는 부모 클래스의 접근 가능한 멤버를 사용할 수 있고, 필요한 경우 메서드를 오버라이딩해서 동작을 바꿀 수 있다. 생성자는 상속되지 않지만 자식 생성자에서 부모 생성자를 호출할 수 있다. 상속은 재사용에 유용하지만 부모와 자식의 결합이 강해질 수 있으므로 역할 관계가 명확할 때 사용하는 것이 좋다."'

post_knowledge \
  "Java Official - Packages" \
  '["java","official","package","packages","namespace","import","패키지","네임스페이스","oracle"]' \
  '"출처: Oracle Java Tutorials - Creating and Using Packages. URL: https://docs.oracle.com/javase/tutorial/java/package/packages.html\n\nJava에서 패키지는 관련 있는 타입을 묶고 이름 충돌을 줄이기 위한 네임스페이스 역할을 한다. 패키지를 사용하면 클래스와 인터페이스를 기능이나 계층에 따라 정리할 수 있고, import 문을 통해 다른 패키지의 타입을 사용할 수 있다. 프로젝트에서는 controller, service, repository, domain처럼 역할별 패키지를 나누어 코드 위치와 책임을 명확히 만드는 데 활용된다."'

post_knowledge \
  "Java Official - Exceptions" \
  '["java","official","exception","exceptions","try","catch","throw","예외","예외처리","oracle"]' \
  '"출처: Oracle Java Tutorials - What Is an Exception. URL: https://docs.oracle.com/javase/tutorial/essential/exceptions/definition.html\n\nJava에서 예외는 프로그램 실행 중 정상적인 흐름을 방해하는 이벤트를 표현한다. 예외가 발생하면 현재 실행 흐름이 중단되고, 호출 스택을 따라 예외를 처리할 수 있는 코드가 있는지 찾는다. try, catch, throw, throws 같은 문법은 예외 상황을 감지하고 처리하거나 호출자에게 전달하기 위해 사용된다. 예외 처리는 오류 상황을 숨기는 것이 아니라 실패 가능성을 명시적으로 다루기 위한 구조다."'
post_knowledge \
  "Java Official - Collection Interface" \
  '["java","official","collection","collections","interface","iterator","foreach","stream","컬렉션","반복","순회","oracle"]' \
  '"출처: Oracle Java Tutorials - The Collection Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/collection.html\n\nJava에서 Collection 인터페이스는 여러 객체를 하나의 그룹으로 다루기 위한 최상위 성격의 컬렉션 인터페이스다. size, isEmpty, contains, add, remove, iterator 같은 기본 연산을 제공하고, addAll, removeAll, clear 같은 bulk operation도 제공한다. 컬렉션을 순회할 때는 for-each, Iterator, stream 같은 방식을 사용할 수 있다. List, Set 같은 하위 컬렉션을 일반적인 방식으로 다루고 싶을 때 Collection 타입을 사용할 수 있다."'

post_knowledge \
  "Java Official - List Interface" \
  '["java","official","collection","list","arraylist","linkedlist","ordered","duplicate","index","리스트","순서","중복","인덱스","oracle"]' \
  '"출처: Oracle Java Tutorials - The List Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html\n\nJava에서 List는 순서가 있는 Collection이며 중복 요소를 허용한다. 각 요소는 인덱스 기반 위치를 가지므로 get, set, add, remove 같은 위치 기반 접근을 사용할 수 있다. 검색에는 indexOf, lastIndexOf 같은 메서드를 사용할 수 있고, 순차적 특성을 활용하는 ListIterator도 제공한다. 일반적인 구현체로 ArrayList와 LinkedList가 있으며, 단순 조회와 끝 추가가 많으면 ArrayList를 먼저 고려하는 경우가 많다."'

post_knowledge \
  "Java Official - Set Interface" \
  '["java","official","collection","set","hashset","treeset","linkedhashset","duplicate","unique","집합","중복제거","유일","oracle"]' \
  '"출처: Oracle Java Tutorials - The Set Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/set.html\n\nJava에서 Set은 중복 요소를 허용하지 않는 Collection이다. 수학의 집합 개념처럼 같은 요소는 한 번만 포함되며, 중복 제거가 필요할 때 사용할 수 있다. 대표 구현체로 HashSet, TreeSet, LinkedHashSet이 있다. HashSet은 일반적으로 빠르지만 순서를 보장하지 않고, TreeSet은 정렬된 순서를 제공하며, LinkedHashSet은 삽입 순서를 유지한다. 구현체보다 Set 인터페이스 타입으로 다루면 구현 교체가 쉬워진다."'

post_knowledge \
  "Java Official - Map Interface" \
  '["java","official","collection","map","hashmap","treemap","linkedhashmap","key","value","entry","맵","키","값","oracle"]' \
  '"출처: Oracle Java Tutorials - The Map Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html\n\nJava에서 Map은 key와 value를 연결하는 객체다. 하나의 key는 최대 하나의 value에 매핑되며, 중복 key를 가질 수 없다. 기본 연산으로 put, get, remove, containsKey, containsValue, size, isEmpty 등을 제공한다. 대표 구현체로 HashMap, TreeMap, LinkedHashMap이 있으며, HashMap은 일반적인 빠른 조회에 많이 쓰이고 TreeMap은 key 정렬이 필요할 때, LinkedHashMap은 입력 순서 유지가 필요할 때 고려할 수 있다."'
