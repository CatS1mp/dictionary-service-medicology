# Dictionary Service API Guide

Tài liệu này mô tả nhanh tác dụng của từng endpoint trong `dictionary-service-medicology`.

Base path:

```text
/api/dictionary
```

Lưu ý:

- Phần lớn endpoint yêu cầu JWT `Bearer token`.
- Các endpoint comment, view, bookmark, vote dùng user hiện tại từ security context.
- Response thực tế phụ thuộc vào `ResponseEntity` hiện có trong code.

## 1. Article APIs

### `POST /api/dictionary/articles`

Tạo mới một article.

Body:

```json
{
  "name": "Hypertension",
  "slug": "hypertension",
  "contentMarkdown": "Noi dung markdown",
  "themeId": "uuid",
  "authorAdminId": "uuid"
}
```

Khi dùng:

- Admin tạo bài viết mới.

### `GET /api/dictionary/articles`

Lấy danh sách toàn bộ article.

Khi dùng:

- Trang quản trị cần xem tất cả bài viết.
- Kiểm tra trạng thái publish và metadata của article.

### `GET /api/dictionary/articles/{id}`

Lấy chi tiết article theo `UUID`.

Khi dùng:

- Màn hình admin detail.
- Cần truy cập bài viết theo id nội bộ.

### `GET /api/dictionary/articles/{slug}`

Lấy article theo `slug`.

Khi dùng:

- Frontend public load bài viết theo slug.

Lưu ý:

- Endpoint này chỉ trả bài đã publish.

### `PUT /api/dictionary/articles/{id}`

Cập nhật article.

Khi dùng:

- Sửa tiêu đề, slug, nội dung markdown, theme, author.

### `DELETE /api/dictionary/articles/{id}`

Xóa article.

Khi dùng:

- Xóa bài viết khỏi hệ thống.

Lưu ý:

- Service hiện dọn link tag và related trước khi xóa article.

### `POST /api/dictionary/articles/{id}/publish`

Publish article.

Khi dùng:

- Chuyển bài viết sang trạng thái public.

### `PATCH /api/dictionary/articles/{id}/unpublish`

Gỡ publish article.

Khi dùng:

- Ẩn bài viết khỏi public nhưng vẫn giữ dữ liệu.

## 2. Tag APIs

### `POST /api/dictionary/tags`

Tạo tag mới.

Body:

```json
{
  "name": "cardiology"
}
```

### `GET /api/dictionary/tags`

Lấy toàn bộ tag.

Khi dùng:

- Dropdown chọn tag.
- Quản lý danh mục tag.

### `GET /api/dictionary/tags/{id}`

Lấy chi tiết một tag theo id.

### `PUT /api/dictionary/tags/{id}`

Cập nhật tên tag.

### `DELETE /api/dictionary/tags/{id}`

Xóa tag.

Lưu ý:

- Service hiện xóa liên kết article-tag trước khi xóa tag.

## 3. Article Tag Management APIs

### `POST /api/dictionary/articles/{id}/tags`

Gán danh sách tag cho article.

Body:

```json
[
  "tag-uuid-1",
  "tag-uuid-2"
]
```

Khi dùng:

- Gán hoặc thay mới toàn bộ danh sách tag của article.

Lưu ý:

- Logic hiện tại sẽ xóa danh sách tag cũ rồi gán lại.
- Tag id trùng sẽ được tự loại bỏ.

### `GET /api/dictionary/articles/{id}/tags`

Lấy danh sách tag của một article.

Khi dùng:

- Hiển thị tag đang gắn vào bài viết.

### `DELETE /api/dictionary/articles/{id}/tags/{tagId}`

Xóa một tag khỏi article.

Khi dùng:

- Gỡ một tag cụ thể mà không cần gán lại cả danh sách.

## 4. Related Article APIs

### `POST /api/dictionary/articles/{id}/related`

Thêm article liên quan.

Body:

```json
{
  "relatedArticleId": "uuid"
}
```

Khi dùng:

- Gợi ý bài viết liên quan ở cuối trang article.

Lưu ý:

- Không cho self-related.
- Quan hệ trùng sẽ không tạo thêm bản ghi mới.

### `GET /api/dictionary/articles/{id}/related`

Lấy danh sách article liên quan của một article.

### `DELETE /api/dictionary/articles/{id}/related/{relatedId}`

Xóa một liên kết related article.

## 5. Comment APIs

### `POST /api/dictionary/articles/{articleId}/comments`

Tạo comment gốc cho article.

Body:

```json
{
  "commentText": "Bai viet rat huu ich"
}
```

Khi dùng:

- Người dùng gửi comment mới cho bài viết.

Lưu ý:

- Comment mới vào trạng thái `PENDING`.

### `GET /api/dictionary/articles/{articleId}/comments`

Lấy danh sách comment đã được duyệt của article.

Khi dùng:

- Hiển thị comment public dưới bài viết.

Lưu ý:

- Chỉ lấy comment `APPROVED`.
- Chỉ reply `APPROVED` mới được trả ra.

### `GET /api/dictionary/comments/{id}`

Lấy chi tiết một comment.

Khi dùng:

- Trang moderation hoặc admin detail.

### `PUT /api/dictionary/comments/{id}`

Cập nhật nội dung comment.

Khi dùng:

- Người tạo comment sửa comment của mình.

### `DELETE /api/dictionary/comments/{id}`

Xóa comment.

Khi dùng:

- Người tạo comment xóa comment của mình.

### `POST /api/dictionary/comments/{id}/reply`

Tạo reply cho một comment.

Khi dùng:

- Trả lời comment gốc hoặc comment con.

Lưu ý:

- Reply mới cũng vào trạng thái `PENDING`.

### `POST /api/dictionary/comments/{id}/approve`

Duyệt comment nhanh sang trạng thái `APPROVED`.

Khi dùng:

- Moderator/admin approve nhanh.

### `PATCH /api/dictionary/comments/{id}/status`

Cập nhật trạng thái moderation của comment.

Body:

```json
{
  "status": "APPROVED"
}
```

Giá trị hỗ trợ:

- `PENDING`
- `APPROVED`
- `REJECTED`
- `HIDDEN`

Khi dùng:

- Workflow moderation đầy đủ thay vì chỉ approve.

### `POST /api/dictionary/comments/{id}/vote`

Vote cho comment.

Body:

```json
{
  "voteType": "UPVOTE"
}
```

Khi dùng:

- Like/dislike hoặc upvote/downvote comment.

Lưu ý:

- Nếu user đã vote trước đó thì vote sẽ được cập nhật.

## 6. Interaction APIs

Base path nhóm này:

```text
/api/dictionary/articles/{articleId}
```

### `POST /view`

Ghi nhận một lượt xem article của user hiện tại.

Khi dùng:

- Mỗi lần user mở article detail.

Lưu ý:

- Nếu user đã từng xem bài viết, hệ thống tăng `viewCount`.

### `GET /views`

Lấy thống kê view của article.

Thông tin chính:

- `totalViews`
- `uniqueViewers`
- `lastViewedAt`

Khi dùng:

- Dashboard hoặc trang thống kê bài viết.

### `POST /bookmark`

Thêm bookmark cho article.

Khi dùng:

- User lưu bài viết để đọc sau.

Lưu ý:

- Nếu đã bookmark rồi thì không tạo thêm bản ghi mới.

### `DELETE /bookmark`

Xóa bookmark của user hiện tại khỏi article.

Khi dùng:

- User bỏ lưu bài viết.

### `GET /interactions/summary`

Lấy summary tương tác của article.

Thông tin chính:

- `totalViews`
- `uniqueViewers`
- `totalBookmarks`
- `totalComments`

Khi dùng:

- Dashboard tổng quan cho article.

## 7. Gợi ý sử dụng theo nghiệp vụ

### Tạo và publish article

1. `POST /articles`
2. `POST /articles/{id}/tags`
3. `POST /articles/{id}/related`
4. `POST /articles/{id}/publish`

### Quản lý moderation comment

1. `GET /comments/{id}`
2. `PATCH /comments/{id}/status`

### Theo dõi tương tác bài viết

1. `POST /articles/{articleId}/view`
2. `POST /articles/{articleId}/bookmark`
3. `GET /articles/{articleId}/interactions/summary`
4. `GET /articles/{articleId}/views`

## 8. Ghi chú hiện trạng

- Auth hiện dựa vào principal trong security context.
- Nếu JWT sau này có user id chuẩn, nên map trực tiếp vào principal để comment/bookmark/view dùng đúng `UUID` gốc.
- Chưa có phân quyền admin/moderator riêng ở controller, nên nếu bạn muốn mình có thể làm tiếp một bản `API_GUIDE_ADMIN.md` mô tả endpoint nào dành cho admin, endpoint nào dành cho user.
