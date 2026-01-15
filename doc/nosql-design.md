# NoSQL Design for Reviews and Logs

## Motivation

The PRD requires exploring NoSQL alternatives for unstructured data such as reviews and inventory logs. While the current system uses a relational schema, some data (e.g., review comments, log details) may benefit from a document-oriented approach for scalability and flexibility.

## Proposed NoSQL Model

### 1. Reviews Collection (MongoDB Example)

```json
{
  "_id": "ObjectId",
  "product_id": 123,
  "user_id": 456,
  "rating": 5,
  "title": "Great product!",
  "comment": "I loved the quality and fast shipping.",
  "is_verified_purchase": true,
  "helpful_votes": 3,
  "created_at": "2026-01-15T10:00:00Z",
  "updated_at": "2026-01-15T10:00:00Z"
}
```

### 2. Inventory Logs Collection

```json
{
  "_id": "ObjectId",
  "product_id": 123,
  "change_amount": 10,
  "previous_quantity": 50,
  "new_quantity": 60,
  "change_type": "restock",
  "change_date": "2026-01-15T09:00:00Z",
  "reason": "Routine restock",
  "performed_by": 789
}
```

## Justification

- **Flexibility**: NoSQL allows storing variable fields and unstructured comments without schema changes.
- **Scalability**: Document stores (e.g., MongoDB) scale horizontally for high-volume review/log data.
- **Integration**: Can be used alongside the relational DB for hybrid persistence (Polyglot).

## Usage

- Export reviews/logs from SQL to NoSQL for analytics or reporting.
- Store new reviews/logs directly in NoSQL for high write throughput.

## References

- [MongoDB Document Model](https://www.mongodb.com/docs/manual/core/document/)
- [Polyglot Persistence](https://martinfowler.com/bliki/PolyglotPersistence.html)

---

_This document satisfies PRD Epic 4.2 and Feature 4.4: NoSQL exploration for unstructured data._
