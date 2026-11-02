# Trang chính sách quyền riêng tư — Ôn Thi GPLX A1

Trang HTML tĩnh dành cho ứng dụng Ôn Thi GPLX A1, nhà phát triển Dam Tien Dat,
email liên hệ damtiendat24072000@gmail.com.

File xuất bản duy nhất là `index.html`. Không cần thư viện, lệnh build, biến môi
trường hay JavaScript. Có thể mở file trực tiếp bằng trình duyệt để xem trước.
Trang dùng font hệ thống, không nhúng công cụ theo dõi hoặc tài nguyên bên ngoài;
các liên kết Google chỉ được mở khi người đọc chọn chúng.

## Netlify — kéo thả

1. Đăng nhập Netlify và mở [Netlify Drop](https://app.netlify.com/drop).
2. Kéo thư mục `privacy-policy` vào vùng deploy. File `index.html` phải nằm ngay
   trong thư mục được kéo vào. Có thể dùng file ZIP được cung cấp nếu bên trong
   ZIP có `index.html` ở cấp đầu tiên.
3. Khi deploy hoàn tất, mở URL HTTPS do Netlify cấp và kiểm tra nội dung.
4. Copy URL trang vừa mở để điền vào ô Privacy policy của Google Play Console.

Nếu kết nối trực tiếp repository Android này với Netlify, đặt **Publish directory**
là `privacy-policy` và để trống **Build command**. Không cần cấu hình chuyển hướng.

Khi cập nhật, deploy lại thư mục vào chính project Netlify đang dùng để giữ URL.

Tham khảo: [Netlify Drop Quickstart](https://docs.netlify.com/start/quickstarts/netlify-drop-quickstart/).

## GitHub Pages — repository riêng

1. Tạo repository công khai, ví dụ `gplx-a1-privacy`.
2. Upload file `index.html` trong thư mục này vào **gốc repository mới**, rồi commit
   trên nhánh `main`. Có thể upload thêm README này.
3. Mở **Settings → Pages → Build and deployment**.
4. Chọn **Source: Deploy from a branch**, **Branch: main**, thư mục **/(root)**,
   rồi **Save**.
5. Chờ GitHub Pages deploy thành công và mở URL do GitHub hiển thị. Với repository
   ví dụ trên, URL thường có dạng `https://<username>.github.io/gplx-a1-privacy/`.
   Thay `<username>` bằng tài khoản GitHub thực tế.
6. Dùng URL trang web đã hoạt động để điền vào Google Play Console. URL xem file
   trên github.com hoặc URL raw không phải URL GitHub Pages cần dùng ở đây.

Nếu repository hiện tại đã bật Pages từ gốc nhánh chứa thư mục `privacy-policy`,
trang nằm tại đường dẫn `privacy-policy/` bên dưới URL Pages của repository đó.
Không thay đổi cấu hình Pages của một website đang hoạt động nếu chưa kiểm tra
ảnh hưởng đến website đó.

Tham khảo: [Cấu hình nguồn xuất bản GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site).

## Trước khi gửi Google Play

- Kiểm tra URL hoạt động trong cửa sổ ẩn danh, không yêu cầu đăng nhập.
- Giữ trang công khai, không đặt mật khẩu hoặc giới hạn khu vực.
- Trang phải là HTML truy cập được; không thay bằng PDF.
- Thêm liên kết hoặc nội dung chính sách trong ứng dụng. Bộ file này chỉ tạo trang
  web, chưa tích hợp liên kết vào ứng dụng Android.
- Khai báo Data safety riêng, phù hợp với chức năng và SDK thực tế; trang chính
  sách không thay thế biểu mẫu Data safety.

Tham khảo: [Chính sách dữ liệu người dùng của Google Play](https://support.google.com/googleplay/android-developer/answer/10144311?hl=vi).

## Cập nhật nội dung

Sửa trực tiếp `index.html`, cập nhật ngày hiển thị và thuộc tính `datetime` trong
thẻ `time`, rồi deploy lại. Nội dung dựa trên mã nguồn hiện tại: lưu tiến độ bằng
SharedPreferences, quảng cáo Google Mobile Ads SDK, hình ảnh từ hoclaixehcm.vn và
sao lưu Android được bật. Rà lại chính sách khi các hành vi này thay đổi.
