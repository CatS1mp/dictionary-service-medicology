create extension if not exists pgcrypto;

insert into components (
    id,
    code,
    name,
    component_type,
    schema_json,
    default_data_json,
    is_active,
    created_at,
    updated_at
) values
(
    gen_random_uuid(),
    'h1_basic',
    'Tiêu đề chính',
    'heading',
    $json$
    {
      "fields": [
        {
          "key": "text",
          "label": "Nội dung",
          "type": "text",
          "required": true,
          "placeholder": "Nhập tiêu đề chính"
        }
      ]
    }
    $json$,
    $json$
    {
      "text": ""
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'h2_basic',
    'Tiêu đề phụ',
    'heading',
    $json$
    {
      "fields": [
        {
          "key": "text",
          "label": "Nội dung",
          "type": "text",
          "required": true,
          "placeholder": "Nhập tiêu đề phụ"
        }
      ]
    }
    $json$,
    $json$
    {
      "text": ""
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'h3_basic',
    'Tiêu đề cấp 3',
    'heading',
    $json$
    {
      "fields": [
        {
          "key": "text",
          "label": "Nội dung",
          "type": "text",
          "required": true,
          "placeholder": "Nhập tiêu đề cấp 3"
        }
      ]
    }
    $json$,
    $json$
    {
      "text": ""
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'paragraph_basic',
    'Đoạn văn',
    'text',
    $json$
    {
      "fields": [
        {
          "key": "text",
          "label": "Nội dung",
          "type": "textarea",
          "required": true,
          "placeholder": "Nhập nội dung đoạn văn"
        }
      ]
    }
    $json$,
    $json$
    {
      "text": ""
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'media_basic',
    'Tệp minh họa',
    'media',
    $json$
    {
      "fields": [
        {
          "key": "file",
          "label": "Thêm tệp",
          "type": "file",
          "accept": [".png", ".jpg", ".jpeg", ".mp4"],
          "maxSizeMb": 5
        },
        {
          "key": "youtubeUrl",
          "label": "Liên kết YouTube",
          "type": "text",
          "placeholder": "https://www.youtube.com/watch?v=..."
        },
        {
          "key": "alt",
          "label": "Văn bản thay thế",
          "type": "text"
        },
        {
          "key": "caption",
          "label": "Chú thích",
          "type": "textarea"
        }
      ]
    }
    $json$,
    $json$
    {
      "kind": "image",
      "url": "",
      "youtubeUrl": "",
      "alt": "",
      "caption": ""
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'warning_basic',
    'Khối cảnh báo',
    'callout',
    $json$
    {
      "fields": [
        {
          "key": "title",
          "label": "Tiêu đề cảnh báo",
          "type": "text",
          "required": true,
          "placeholder": "Nhập tiêu đề cảnh báo"
        },
        {
          "key": "level",
          "label": "Mức cảnh báo",
          "type": "select",
          "options": ["low", "medium", "high"],
          "required": true
        },
        {
          "key": "items",
          "label": "Danh sách ý",
          "type": "list_text",
          "placeholder": "Mỗi dòng là một ý cảnh báo"
        }
      ]
    }
    $json$,
    $json$
    {
      "title": "",
      "level": "high",
      "items": []
    }
    $json$,
    true,
    now(),
    now()
),
(
    gen_random_uuid(),
    'term_box_basic',
    'Hộp thuật ngữ',
    'reference',
    $json$
    {
      "fields": [
        {
          "key": "term",
          "label": "Thuật ngữ",
          "type": "text",
          "required": true,
          "placeholder": "Nhập thuật ngữ"
        },
        {
          "key": "definition",
          "label": "Định nghĩa",
          "type": "textarea",
          "required": true,
          "placeholder": "Nhập định nghĩa ngắn"
        }
      ]
    }
    $json$,
    $json$
    {
      "term": "",
      "definition": ""
    }
    $json$,
    true,
    now(),
    now()
);
