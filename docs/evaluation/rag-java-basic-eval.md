# Java Basic RAG Evaluation Set

## Goal

This evaluation set checks whether OLCA can retrieve the correct official Java knowledge document for basic Java questions.

The current target is not perfect answer generation. The target is a minimum retrieval quality that is safe enough for early conversation tests.

## Pass Criteria

- Top1 accuracy target: 70% or higher
- Top3 inclusion target: 85% or higher
- Unknown-topic questions must return no confident official document or be handled safely by the RAG answer flow

## Evaluation Cases

| id | question | expectedTop1 | allowedTop3 | type |
|---|---|---|---|---|
| java-basic-001 | 자바 클래스와 객체 차이 알려줘 | Java Official - Classes and Objects | Java Official - Classes and Objects | java-basic |
| java-basic-002 | 자바에서 객체는 뭐야? | Java Official - Classes and Objects | Java Official - Classes and Objects | java-basic |
| java-basic-003 | 클래스는 왜 쓰는 거야? | Java Official - Classes and Objects | Java Official - Classes and Objects | java-basic |
| java-basic-004 | 자바 인터페이스는 언제 써? | Java Official - Interfaces | Java Official - Interfaces | java-basic |
| java-basic-005 | 인터페이스와 구현체 관계 알려줘 | Java Official - Interfaces | Java Official - Interfaces | java-basic |
| java-basic-006 | API 계약을 자바에서 어떻게 표현해? | Java Official - Interfaces | Java Official - Interfaces | java-basic |
| java-basic-007 | 자바 상속은 언제 쓰는 게 좋아? | Java Official - Inheritance | Java Official - Inheritance | java-basic |
| java-basic-008 | 부모 클래스와 자식 클래스 관계 설명해줘 | Java Official - Inheritance | Java Official - Inheritance | java-basic |
| java-basic-009 | 메서드 오버라이딩은 상속이랑 무슨 관련이 있어? | Java Official - Inheritance | Java Official - Inheritance | java-basic |
| java-basic-010 | 자바 패키지는 왜 나눠? | Java Official - Packages | Java Official - Packages | java-basic |
| java-basic-011 | import 문은 왜 필요해? | Java Official - Packages | Java Official - Packages | java-basic |
| java-basic-012 | 패키지가 이름 충돌을 줄여준다는 게 무슨 말이야? | Java Official - Packages | Java Official - Packages | java-basic |
| java-basic-013 | 자바 예외 처리는 왜 필요해? | Java Official - Exceptions | Java Official - Exceptions | java-basic |
| java-basic-014 | try catch는 언제 써? | Java Official - Exceptions | Java Official - Exceptions | java-basic |
| java-basic-015 | throw와 throws는 예외 처리랑 관련 있어? | Java Official - Exceptions | Java Official - Exceptions | java-basic |
| design-pattern-001 | 빌더 패턴은 언제 써? | Builder Pattern - 사용 시점 | Builder Pattern - 사용 시점, Builder Pattern, Builder vs Factory vs Singleton | design-pattern |
| design-pattern-002 | 팩토리 패턴은 언제 쓰는 게 좋아? | Factory Pattern - 사용 시점 | Factory Pattern - 사용 시점, Factory Pattern, Builder vs Factory vs Singleton | design-pattern |
| design-pattern-003 | 싱글톤 패턴은 언제 써? | Singleton Pattern - 사용 시점 | Singleton Pattern - 사용 시점, Singleton Pattern, Builder vs Factory vs Singleton | design-pattern |
| unknown-001 | 스트림 고블린 패턴 알려줘 | NONE | NONE | unknown |
| unknown-002 | 커피 추출 온도 알려줘 | NONE | NONE | unknown |

## Notes

This set is intentionally small. The goal is to prevent repetitive manual testing and create a baseline before adding more official documents.

