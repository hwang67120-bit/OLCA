#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

post_knowledge() {
  local topic="$1"
  local keywords="$2"
  local content="$3"
  local metadata="${4:-{}}"

  curl -sS -X POST "${BASE_URL}/api/knowledge" \
    -H "Content-Type: application/json" \
    -d "{
      \"topic\": \"${topic}\",
      \"keywords\": ${keywords},
      \"metadata\": ${metadata},
      \"content\": ${content}
    }"
  printf '\n'
}

post_knowledge \
  "Java Official - Collection Interface" \
  '["java","official","collection","collections","interface","iterator","foreach","stream","컬렉션","반복","순회","oracle"]' \
  '"출처: Oracle Java Tutorials - The Collection Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/collection.html\n\nJava에서 Collection 인터페이스는 여러 객체를 하나의 그룹으로 다루기 위한 최상위 성격의 컬렉션 인터페이스다. size, isEmpty, contains, add, remove, iterator 같은 기본 연산을 제공하고, addAll, removeAll, clear 같은 bulk operation도 제공한다. 컬렉션을 순회할 때는 for-each, Iterator, stream 같은 방식을 사용할 수 있다. List, Set 같은 하위 컬렉션을 일반적인 방식으로 다루고 싶을 때 Collection 타입을 사용할 수 있다."' \
  '{"sourceType":"OFFICIAL_DOCS","sourceName":"Oracle Java Tutorials - Collection Interface","sourceUrl":"https://docs.oracle.com/javase/tutorial/collections/interfaces/collection.html","domain":"java","category":"collection","topicKey":"java-collection-interface"}'

post_knowledge \
  "Java Official - List Interface" \
  '["java","official","collection","list","arraylist","linkedlist","ordered","duplicate","index","리스트","순서","중복","인덱스","oracle"]' \
  '"출처: Oracle Java Tutorials - The List Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html\n\nJava에서 List는 순서가 있는 Collection이며 중복 요소를 허용한다. 각 요소는 인덱스 기반 위치를 가지므로 get, set, add, remove 같은 위치 기반 접근을 사용할 수 있다. 검색에는 indexOf, lastIndexOf 같은 메서드를 사용할 수 있고, 순차적 특성을 활용하는 ListIterator도 제공한다. 일반적인 구현체로 ArrayList와 LinkedList가 있으며, 단순 조회와 끝 추가가 많으면 ArrayList를 먼저 고려하는 경우가 많다."' \
  '{"sourceType":"OFFICIAL_DOCS","sourceName":"Oracle Java Tutorials - List Interface","sourceUrl":"https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html","domain":"java","category":"collection","topicKey":"java-list-interface"}'

post_knowledge \
  "Java Official - Set Interface" \
  '["java","official","collection","set","hashset","treeset","linkedhashset","duplicate","unique","집합","중복제거","유일","oracle"]' \
  '"출처: Oracle Java Tutorials - The Set Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/set.html\n\nJava에서 Set은 중복 요소를 허용하지 않는 Collection이다. 수학의 집합 개념처럼 같은 요소는 한 번만 포함되며, 중복 제거가 필요할 때 사용할 수 있다. 대표 구현체로 HashSet, TreeSet, LinkedHashSet이 있다. HashSet은 일반적으로 빠르지만 순서를 보장하지 않고, TreeSet은 정렬된 순서를 제공하며, LinkedHashSet은 삽입 순서를 유지한다. 구현체보다 Set 인터페이스 타입으로 다루면 구현 교체가 쉬워진다."' \
  '{"sourceType":"OFFICIAL_DOCS","sourceName":"Oracle Java Tutorials - Set Interface","sourceUrl":"https://docs.oracle.com/javase/tutorial/collections/interfaces/set.html","domain":"java","category":"collection","topicKey":"java-set-interface"}'

post_knowledge \
  "Java Official - Map Interface" \
  '["java","official","collection","map","hashmap","treemap","linkedhashmap","key","value","entry","맵","키","값","oracle"]' \
  '"출처: Oracle Java Tutorials - The Map Interface. URL: https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html\n\nJava에서 Map은 key와 value를 연결하는 객체다. 하나의 key는 최대 하나의 value에 매핑되며, 중복 key를 가질 수 없다. 기본 연산으로 put, get, remove, containsKey, containsValue, size, isEmpty 등을 제공한다. 대표 구현체로 HashMap, TreeMap, LinkedHashMap이 있으며, HashMap은 일반적인 빠른 조회에 많이 쓰이고 TreeMap은 key 정렬이 필요할 때, LinkedHashMap은 입력 순서 유지가 필요할 때 고려할 수 있다."' \
  '{"sourceType":"OFFICIAL_DOCS","sourceName":"Oracle Java Tutorials - Map Interface","sourceUrl":"https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html","domain":"java","category":"collection","topicKey":"java-map-interface"}'