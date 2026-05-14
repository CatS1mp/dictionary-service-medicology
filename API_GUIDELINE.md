# Dictionary Service API Guideline

## 1. Mục đích tài liệu

Tài liệu này mô tả endpoint trong `dictionary-service-medicology`, gồm:

- Công dụng của endpoint
- Input request
- Output response
- Màn hình hoặc flow nên sử dụng
- Lưu ý triển khai FE/BE

## 2. Base URL và tài liệu kỹ thuật

- Local: `http://localhost:8082`
- Staging: `Chưa cấu hình riêng trong repo`
- Production: `https://dictionary-service-medicology-2eca7b389d41.herokuapp.com`

Swagger / OpenAPI:

- `GET /swagger-ui.html`
- `GET /api-docs`
- `GET /` redirect sang Swagger

## 3. Authentication và quy ước chung

### 3.1 Authentication

- Public: `/`, Swagger
- Tất cả endpoint business dưới `/api/dictionary/**` hiện yêu cầu JWT Bearer ở security layer
- Một số endpoint được siết thêm `ROLE_ADMIN` bằng `@PreAuthorize`, nhưng không phải tất cả action quản trị đều đã gắn guard

Header chuẩn:

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### 3.2 Kiểu lỗi

Format lỗi hiện tại:

```json
{
  "status": 401,
  "message": "Mô tả lỗi",
  "timestamp": "2026-04-13T10:30:00"
}
```

Mapping chính:

- `IllegalArgumentException` đang bị map thành `401 Unauthorized`
- Validation trả `400 Bad Request`
- `ResponseStatusException` giữ nguyên status do service ném ra
- Lỗi chưa bắt riêng trả `500 Internal Server Error`

### 3.3 Quy ước response

- Chưa có wrapper chung kiểu `ApiResponse<T>`
- Endpoint create có thể trả `UUID` hoặc DTO tùy nhóm API
- Endpoint update/delete thường trả `200 OK` với body rỗng
- Các endpoint read trả list/DTO trực tiếp

### 3.4 Chuẩn dữ liệu `contentJson` cho bài viết

- Dữ liệu lưu trong `contentJson` theo dạng:

```json
{
  "version": 2,
  "blocks": [
    {
      "id": "uuid-or-string",
      "componentCode": "H2",
      "componentType": "heading",
      "name": "Tiêu đề mục",
      "level": 2,
      "data": { "content": "..." }
    }
  ]
}
```

- **Breaking change:** từ bản này, payload cũ không đạt schema mới sẽ bị từ chối.
- `contentJson.version` bắt buộc = `2`.
- `blocks[]` bắt buộc là mảng object; mỗi block phải có đầy đủ:
  - `id` (string, không rỗng)
  - `componentCode` (string, không rỗng)
  - `componentType` (string, không rỗng)
  - `name` (string, không rỗng)
  - `level` (integer trong khoảng `1..3`)
  - `data` (object JSON)
- `contentVersion` trong `ArticleRequest` bắt buộc = `2`.
- Heading hiển thị thực tế do FE suy luận từ `componentCode/componentType/data` + `level`, BE không transform nội dung.

## 4. Tóm tắt mapping theo màn hình

| Màn hình / flow | Endpoint chính |
| --- | --- |
| Danh sách bài viết cho admin | `GET /api/dictionary/articles` |
| Chi tiết bài viết theo slug | `GET /api/dictionary/articles/{slug}` |
| CMS tạo/sửa/xóa bài viết | `POST/PUT/DELETE /api/dictionary/articles...` |
| Gắn tag và bài liên quan | `POST /api/dictionary/articles/{id}/tags`, `POST /api/dictionary/articles/{id}/related` |
| Quản lý tag | `GET /api/dictionary/tags`, `POST/PUT/DELETE /api/dictionary/tags...` |
| Danh sách bình luận của bài | `GET /api/dictionary/articles/{articleId}/comments` |
| Viết bình luận / reply / vote | `POST /api/dictionary/articles/{articleId}/comments`, `POST /api/dictionary/comments/{id}/reply`, `POST /api/dictionary/comments/{id}/vote` |
| Moderation comment | `POST /api/dictionary/comments/{id}/approve`, `PATCH /api/dictionary/comments/{id}/status` |
| Bookmark và tương tác bài viết | `POST/DELETE /api/dictionary/articles/{articleId}/bookmark`, `POST /api/dictionary/articles/{articleId}/view` |
| Danh sách bookmark của tôi | `GET /api/dictionary/users/me/bookmarks` |
| Upload ảnh infographic cho admin | `POST /api/dictionary/admin/assets` + `GET /api/dictionary/assets/{fileName}` |

## 5. Nhóm API — Article và taxonomy

### 5.1 Bài viết, tag, related article

- `POST /api/dictionary/articles`
  - **Mục đích:** Tạo bài viết mới
  - **Body:** `ArticleRequest`
  - **Response:** `200 OK`, `UUID` của article mới
  - **Validation bắt buộc:** `contentVersion=2` và `contentJson` đạt schema v2 (nếu sai trả `400`)
  - **Ghi chú:** Có `@PreAuthorize("hasRole('ADMIN')")`
- `GET /api/dictionary/articles`
  - **Mục đích:** Lấy danh sách toàn bộ article
  - **Response:** `200 OK`, `List<ArticleResponse>`
- `GET /api/dictionary/articles/{id}`
  - **Mục đích:** Lấy chi tiết article theo UUID
  - **Query / Path:** `id=<UUID>`
  - **Response:** `200 OK`, `ArticleResponse`
- `GET /api/dictionary/articles/{slug}`
  - **Mục đích:** Lấy article theo slug
  - **Query / Path:** `slug=<slug>`
  - **Response:** `200 OK`, `ArticleResponse`
  - **Ghi chú:** Do security config hiện tại, route này vẫn cần JWT
- `PUT /api/dictionary/articles/{id}`
  - **Mục đích:** Cập nhật bài viết
  - **Body:** `ArticleRequest`
  - **Response:** `200 OK`, body rỗng
  - **Validation bắt buộc:** `contentVersion=2` và `contentJson` đạt schema v2 (nếu sai trả `400`)
  - **Ghi chú:** Có guard admin
- `DELETE /api/dictionary/articles/{id}`
  - **Mục đích:** Xóa bài viết
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `POST /api/dictionary/articles/{id}/publish`
  - **Mục đích:** Publish bài viết
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `PATCH /api/dictionary/articles/{id}/unpublish`
  - **Mục đích:** Gỡ publish bài viết
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Hiện controller chưa gắn guard admin, nên mọi user đã đăng nhập đều gọi được
- `POST /api/dictionary/articles/{id}/tags`
  - **Mục đích:** Gán danh sách tag cho article
  - **Body:** `List<UUID>`
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `GET /api/dictionary/articles/{id}/tags`
  - **Mục đích:** Lấy tag của bài viết
  - **Response:** `200 OK`, `List<TagResponse>`
- `DELETE /api/dictionary/articles/{id}/tags/{tagId}`
  - **Mục đích:** Gỡ một tag khỏi bài viết
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `POST /api/dictionary/articles/{id}/related`
  - **Mục đích:** Thêm article liên quan
  - **Body:** `RelatedArticleRequest`
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `GET /api/dictionary/articles/{id}/related`
  - **Mục đích:** Lấy danh sách article liên quan
  - **Response:** `200 OK`, `List<ArticleResponse>`
- `DELETE /api/dictionary/articles/{id}/related/{relatedId}`
  - **Mục đích:** Xóa liên kết bài liên quan
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `POST /api/dictionary/admin/assets`
  - **Mục đích:** Upload file ảnh phục vụ block infographic/media trong editor
  - **Body:** `multipart/form-data`, field bắt buộc `file`
  - **Response:** `200 OK`, `DictionaryAssetUploadResponse`
    - `assetId`: id nội bộ của file
    - `url`: đường dẫn dùng trực tiếp trong `block.data.imageUrl`
    - `fileName`, `contentType`, `sizeBytes`
  - **Validation:** chỉ cho MIME image (`png/jpeg/webp/gif`), giới hạn kích thước theo config
  - **Auth:** `ROLE_ADMIN`
  - **Supabase (tuỳ chọn):** Nếu cấu hình `DICTIONARY_SUPABASE_URL`, `DICTIONARY_SUPABASE_BUCKET`, `DICTIONARY_SUPABASE_SERVICE_ROLE_KEY` (xem `application.properties`), file được đẩy lên Storage bucket và `url` trả về là URL public `.../storage/v1/object/public/...`. Bucket cần **public** (hoặc dùng URL signed ở vòng sau). Nếu thiếu cấu hình Supabase, service ghi file **local** như cũ và `url` trỏ tới `GET /api/dictionary/assets/{fileName}`.
- `GET /api/dictionary/assets/{fileName}`
  - **Mục đích:** Trả binary asset đã upload
  - **Response:** `200 OK` với content-type tương ứng, body là file
- `POST /api/dictionary/tags`
  - **Mục đích:** Tạo tag
  - **Body:** `TagRequest`
  - **Response:** `200 OK`, `TagResponse`
  - **Ghi chú:** Có guard admin
- `GET /api/dictionary/tags`
  - **Mục đích:** Lấy toàn bộ tag
  - **Response:** `200 OK`, `List<TagResponse>`
- `GET /api/dictionary/tags/{id}`
  - **Mục đích:** Lấy chi tiết tag
  - **Response:** `200 OK`, `TagResponse`
- `PUT /api/dictionary/tags/{id}`
  - **Mục đích:** Cập nhật tag
  - **Body:** `TagRequest`
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `DELETE /api/dictionary/tags/{id}`
  - **Mục đích:** Xóa tag
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin

## 6. Nhóm API — Comment và moderation

### 6.1 Bình luận, phản hồi, vote

- `POST /api/dictionary/articles/{articleId}/comments`
  - **Mục đích:** Tạo comment gốc cho bài viết
  - **Body:** `CommentRequest`
  - **Response:** `200 OK`, `UUID` comment mới
- `GET /api/dictionary/articles/{articleId}/comments`
  - **Mục đích:** Lấy danh sách comment của bài viết
  - **Response:** `200 OK`, `List<CommentResponse>`
- `POST /api/dictionary/comments/{id}/reply`
  - **Mục đích:** Tạo reply cho comment
  - **Body:** `CommentRequest`
  - **Response:** `200 OK`, `UUID` reply mới
- `GET /api/dictionary/comments/{id}`
  - **Mục đích:** Lấy chi tiết một comment
  - **Response:** `200 OK`, `CommentResponse`
- `PUT /api/dictionary/comments/{id}`
  - **Mục đích:** Cập nhật comment
  - **Body:** `CommentRequest`
  - **Response:** `200 OK`, body rỗng
- `DELETE /api/dictionary/comments/{id}`
  - **Mục đích:** Xóa comment
  - **Response:** `200 OK`, body rỗng
- `POST /api/dictionary/comments/{id}/approve`
  - **Mục đích:** Duyệt nhanh comment
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Có guard admin
- `PATCH /api/dictionary/comments/{id}/status`
  - **Mục đích:** Đổi trạng thái moderation
  - **Body:** `CommentStatusRequest`
  - **Response:** `200 OK`, body rỗng
  - **Ghi chú:** Hiện controller chưa gắn guard admin
- `POST /api/dictionary/comments/{id}/vote`
  - **Mục đích:** Vote cho comment
  - **Body:** `VoteRequest`
  - **Response:** `200 OK`, body rỗng

## 7. Nhóm API — Interaction và bookmark

### 7.1 Theo dõi hành vi người dùng

- `POST /api/dictionary/articles/{articleId}/view`
  - **Mục đích:** Ghi nhận lượt xem bài viết
  - **Response:** `200 OK`, body rỗng
- `POST /api/dictionary/articles/{articleId}/bookmark`
  - **Mục đích:** Lưu bookmark bài viết
  - **Response:** `200 OK`, body rỗng
- `DELETE /api/dictionary/articles/{articleId}/bookmark`
  - **Mục đích:** Bỏ bookmark của user hiện tại
  - **Response:** `200 OK`, body rỗng
- `GET /api/dictionary/articles/{articleId}/interactions/summary`
  - **Mục đích:** Lấy summary tương tác của một bài viết
  - **Response:** `200 OK`, `InteractionSummaryResponse`
- `GET /api/dictionary/articles/{articleId}/views`
  - **Mục đích:** Lấy thống kê view của bài viết
  - **Response:** `200 OK`, `ViewStatisticsResponse`
- `GET /api/dictionary/users/me/bookmarks`
  - **Mục đích:** Lấy danh sách bookmark của user hiện tại
  - **Response:** `200 OK`, `List<ArticleResponse>`

## 8. Webhook / callback (nếu có)

- Không áp dụng

## 9. Hợp đồng với service khác

- Dùng JWT do auth service cấp để nhận diện user hiện tại
- Không thấy outbound API call sang service backend khác ở controller layer
- Một số API read-only cho public content hiện vẫn đang bị chặn bởi security config, nên nếu website cần public article detail theo slug thì cần nới contract auth trước

---

*Cập nhật lần cuối: 2026-04-13 — Backend team*
