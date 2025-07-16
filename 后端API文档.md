# 后端API文档

## 项目概述
- **项目名称**: www-springboot
- **技术栈**: Spring Boot 3.2.5 + Spring Security + JPA + MySQL
- **Java版本**: 1.8 (注意：Spring Boot 3.x通常需要Java 17+，当前配置可能存在兼容性问题)
- **端口**: 8082
- **基础URL**: `http://localhost:8082`

## 数据库配置
- **数据库**: MySQL
- **数据库名**: www
- **用户名**: root
- **密码**: 123456
- **端口**: 3306
- **连接URL**: `jdbc:mysql://localhost:3306/www?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC`

## 用户表结构
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_type VARCHAR(255) NOT NULL DEFAULT 'USER',
    avatar LONGTEXT
);
```

## API接口列表

### 1. 用户注册
- **接口地址**: `POST /api/user/register`
- **请求头**: `Content-Type: application/json`
- **请求参数**:
```json
{
    "username": "string",     // 用户名，必填，唯一
    "password": "string",     // 密码，必填
    "phone": "string",        // 手机号，必填，唯一
    "email": "string",        // 邮箱，必填，唯一
    "userType": "string",     // 用户类型，可选，默认为"USER"，只能是"ADMIN"或"USER"
    "avatar": "string"        // 头像，可选，base64字符串或图片URL
}
```

- **成功响应** (200):
```json
"注册成功"
```

- **失败响应** (400):
```json
"用户名已存在"
"手机号已注册"
"邮箱已注册"
```

### 2. 用户登录
- **接口地址**: `POST /api/user/login`
- **请求头**: `Content-Type: application/json`
- **请求参数**:
```json
{
    "username": "string",     // 用户名，必填
    "password": "string"      // 密码，必填
}
```

- **成功响应** (200):
```json
"eyJhbGciOiJIUzI1NiJ9..."  // JWT Token字符串
```

- **失败响应** (400):
```json
"用户名或密码错误"
```

### 3. 获取用户信息
- **接口地址**: `GET /api/user/info`
- **请求头**: 
  - `Authorization: Bearer {token}` (必填)
- **请求参数**: 无

- **成功响应** (200):
```json
{
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "userType": "USER",
    "avatar": "https://img1.baidu.com/it/u=1618033988,3141592653&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=200"
}
```

- **失败响应** (401):
```json
"未登录或token缺失"
"token无效"
```

- **失败响应** (400):
```json
"用户不存在"
```

### 4. 更新用户头像
- **接口地址**: `POST /api/user/avatar` 或 `POST /api/user/updateAvatar`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (必填)
- **请求参数**:
```json
{
    "avatar": "string"        // 头像，必填，base64字符串或图片URL
}
```

- **成功响应** (200):
```json
"头像更新成功"
```

- **失败响应** (401):
```json
"未登录或token缺失"
"token无效"
```

- **失败响应** (400):
```json
"用户不存在"
"头像数据不能为空"
```

### 5. 更新用户信息
- **接口地址**: `POST /api/user/update`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (必填)
- **请求参数**:
```json
{
    "username": "string",     // 用户名，可选
    "phone": "string",        // 手机号，可选
    "email": "string",        // 邮箱，可选
    "userType": "string",     // 用户类型，可选，只能是"ADMIN"或"USER"
    "avatar": "string"        // 头像，可选，base64字符串或图片URL
}
```

- **成功响应** (200):
```json
"用户信息更新成功"
```

- **失败响应** (401):
```json
"未登录或token缺失"
"token无效"
```

- **失败响应** (400):
```json
"用户不存在"
"用户名已存在"
"手机号已注册"
"邮箱已注册"
"用户类型只能是ADMIN或USER"
```

- **失败响应** (500):
```json
"保存失败: {具体错误信息}"
```

### 6. 修改密码
- **接口地址**: `POST /api/user/changePwd`
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (必填)
- **请求参数**:
```json
{
    "oldPwd": "string",       // 原密码，必填
    "newPwd": "string"        // 新密码，必填
}
```

- **成功响应** (200):
```json
"密码修改成功"
```

- **失败响应** (401):
```json
"未登录或token缺失"
"token无效"
```

- **失败响应** (400):
```json
"用户不存在"
"原密码错误"
"原密码不能为空"
"新密码不能为空"
```

- **失败响应** (500):
```json
"数据库密码格式异常，请联系管理员"
"密码修改失败: {具体错误信息}"
```

### 7. 星火大模型代理接口
- **接口地址**: `POST /api/sparkai`
- **请求头**: 
  - `Content-Type: application/json`
- **请求参数**:
```json
{
    "model": "x1",           // 可选，模型名称，支持 x1、pro、lite、max、ultra，默认x1
    "messages": [             // 必填，对话消息数组，格式同OpenAI
        {"role": "user", "content": "你好，AI！"}
    ]
}
```
- **成功响应** (200):
```json
{
    // 由星火API返回的原始响应内容，结构与讯飞星火API一致
}
```
- **失败响应** (400):
```json
{"error": "不支持的模型: xxx"}
{"error": "消息不能为空"}
```
- **失败响应** (500):
```json
{"error": "请求失败: {具体错误信息}"}
```

#### 支持的模型
| model  | 说明           |
|--------|----------------|
| x1     | 星火X1（v2接口）|
| pro    | 星火Pro（v1接口）|
| lite   | 星火Lite（v1接口，暂用Pro配置）|
| max    | 星火Max（v1接口，暂用Pro配置）|
| ultra  | 星火Ultra（v1接口，暂用Pro配置）|

#### 示例请求
```bash
curl -X POST http://localhost:8082/api/sparkai \
  -H "Content-Type: application/json" \
  -d '{
    "model": "x1",
    "messages": [
      {"role": "user", "content": "你好，星火！"}
    ]
  }'
```

## 安全配置
- **CSRF**: 已禁用
- **认证**: 注册和登录接口无需认证，其他接口需要JWT Token认证
- **密码加密**: 使用BCrypt算法加密存储
- **密码验证**: 使用BCrypt.matches()方法进行密码验证
- **CORS**: 已配置，允许以下前端域名访问：
  - `http://localhost:1234`
  - `http://127.0.0.1:3000`
  - `http://localhost:3000`
  - `http://127.0.0.1:8080`
  - `http://localhost:8080`

## JWT Token说明
- **算法**: HS256
- **有效期**: 24小时
- **密钥**: mySecretKey123456
- **包含信息**: 用户名 + 用户ID（新版本）
- **使用方式**: 在请求头中添加 `Authorization: Bearer {token}`

### Token版本说明
- **新版本Token**: 包含用户ID和用户名，修改用户名后token依然有效
- **旧版本Token**: 只包含用户名，修改用户名后需要重新登录
- **兼容性**: 后端同时支持两种token格式，优先使用用户ID查找用户

### Token升级建议
1. 用户重新登录后自动获取新版本token
2. 新版本token修改用户名后无需重新登录
3. 旧版本token仍可正常使用，但建议升级

## 错误码说明
- **200**: 请求成功
- **400**: 请求参数错误或业务逻辑错误
- **401**: 未认证或认证失败
- **500**: 服务器内部错误

## 技术细节
1. **密码存储**: 使用BCrypt算法，哈希长度为60字符，格式为`$2a$...`
2. **数据库操作**: 使用JPA/Hibernate，自动创建表结构
3. **日志输出**: 所有接口都有详细的调试日志输出
4. **参数验证**: 服务端进行完整的参数验证和业务逻辑检查
5. **异常处理**: 统一的异常处理机制，返回友好的错误信息

## 注意事项
1. 所有请求和响应均使用JSON格式
2. 密码在传输和存储时都会进行BCrypt加密处理
3. 用户名、手机号、邮箱都具有唯一性约束
4. 用户类型默认为"USER"，可选值为"ADMIN"或"USER"
5. 头像字段支持base64字符串或图片URL格式
6. 注册时如果未提供头像，系统会自动设置默认头像
7. 需要认证的接口必须在请求头中携带有效的JWT Token
8. 系统支持新旧版本token的兼容性，但建议使用新版本token

## 开发环境启动
1. 确保MySQL服务已启动
2. 创建名为"www"的数据库
3. 启动Spring Boot应用
4. 访问 `http://localhost:8082` 验证服务是否正常

## 兼容性说明
- **Java版本**: 当前配置使用Java 1.8，但Spring Boot 3.2.5通常需要Java 17+
- **建议升级**: 考虑升级到Java 17+以获得更好的性能和兼容性
- **JWT库**: 使用io.jsonwebtoken:jjwt:0.9.1版本

## 测试示例

### 注册用户
```bash
curl -X POST http://localhost:8082/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "phone": "13800138000",
    "email": "test@example.com",
    "userType": "USER",
    "avatar": "https://example.com/avatar.jpg"
  }'
```

### 用户登录
```bash
curl -X POST http://localhost:8082/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 获取用户信息
```bash
curl -X GET http://localhost:8082/api/user/info \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 更新用户头像
```bash
curl -X POST http://localhost:8082/api/user/avatar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "avatar": "https://example.com/new-avatar.jpg"
  }'
```

### 更新用户信息
```bash
curl -X POST http://localhost:8082/api/user/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "username": "newusername",
    "phone": "13900139000",
    "email": "newemail@example.com",
    "userType": "USER",
    "avatar": "https://example.com/avatar.jpg"
  }'
```

### 修改密码
```bash
curl -X POST http://localhost:8082/api/user/changePwd \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "oldPwd": "123456",
    "newPwd": "newpassword123"
  }'
```

### 星火大模型代理接口
```bash
curl -X POST http://localhost:8082/api/sparkai \
  -H "Content-Type: application/json" \
  -d '{
    "model": "x1",
    "messages": [
      {"role": "user", "content": "你好，星火！"}
    ]
  }'
```

## 前端集成建议
1. 登录成功后保存JWT Token到localStorage或sessionStorage
2. 在需要认证的请求中添加Authorization请求头
3. 处理401错误时跳转到登录页面
4. 可以使用axios拦截器统一处理Token的添加和错误处理

### 前端接口调用示例

#### 1. 用户信息管理
```javascript
// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await axios.get('/api/user/info', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    return response.data;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    throw error;
  }
};

// 更新用户信息
const updateUserInfo = async (userData) => {
  try {
    const response = await axios.post('/api/user/update', userData, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error;
  }
};

// 更新头像
const updateAvatar = async (avatarUrl) => {
  try {
    const response = await axios.post('/api/user/avatar', { avatar: avatarUrl }, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('更新头像失败:', error);
    throw error;
  }
};
```

#### 2. 密码管理
```javascript
// 修改密码
const changePassword = async (oldPassword, newPassword) => {
  try {
    const response = await axios.post('/api/user/changePwd', {
      oldPwd: oldPassword,
      newPwd: newPassword
    }, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('修改密码失败:', error);
    throw error;
  }
};
```

#### 3. Axios拦截器配置
```javascript
// 请求拦截器 - 自动添加token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一处理错误
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token过期或无效，跳转到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 用户信息编辑页面示例
```javascript
// 用户信息编辑组件
const UserProfileEdit = () => {
  const [userInfo, setUserInfo] = useState({});
  const [loading, setLoading] = useState(false);

  // 获取用户信息
  useEffect(() => {
    getUserInfo().then(data => setUserInfo(data));
  }, []);

  // 保存用户信息
  const handleSave = async (formData) => {
    setLoading(true);
    try {
      await updateUserInfo(formData);
      message.success('用户信息更新成功');
      // 重新获取用户信息
      const newUserInfo = await getUserInfo();
      setUserInfo(newUserInfo);
    } catch (error) {
      message.error(error.response?.data || '更新失败');
    } finally {
      setLoading(false);
    }
  };

  // 更新头像
  const handleAvatarChange = async (avatarUrl) => {
    try {
      await updateAvatar(avatarUrl);
      message.success('头像更新成功');
      // 重新获取用户信息
      const newUserInfo = await getUserInfo();
      setUserInfo(newUserInfo);
    } catch (error) {
      message.error('头像更新失败');
    }
  };

  return (
    <div>
      {/* 用户信息表单 */}
      <Form onFinish={handleSave} initialValues={userInfo}>
        <Form.Item label="用户名" name="username">
          <Input />
        </Form.Item>
        <Form.Item label="手机号" name="phone">
          <Input />
        </Form.Item>
        <Form.Item label="邮箱" name="email">
          <Input />
        </Form.Item>
        <Form.Item label="用户类型" name="userType">
          <Select>
            <Option value="USER">普通用户</Option>
            <Option value="ADMIN">管理员</Option>
          </Select>
        </Form.Item>
        <Button type="primary" htmlType="submit" loading={loading}>
          保存
        </Button>
      </Form>
    </div>
  );
};
```

### 密码修改页面示例
```javascript
// 密码修改组件
const ChangePassword = () => {
  const [loading, setLoading] = useState(false);

  const handleChangePassword = async (values) => {
    setLoading(true);
    try {
      await changePassword(values.oldPassword, values.newPassword);
      message.success('密码修改成功');
      // 清空表单
      form.resetFields();
    } catch (error) {
      message.error(error.response?.data || '密码修改失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form onFinish={handleChangePassword}>
      <Form.Item
        label="原密码"
        name="oldPassword"
        rules={[{ required: true, message: '请输入原密码' }]}
      >
        <Input.Password />
      </Form.Item>
      <Form.Item
        label="新密码"
        name="newPassword"
        rules={[{ required: true, message: '请输入新密码' }]}
      >
        <Input.Password />
      </Form.Item>
      <Form.Item
        label="确认新密码"
        name="confirmPassword"
        rules={[
          { required: true, message: '请确认新密码' },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('newPassword') === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error('两次输入的密码不一致'));
            },
          }),
        ]}
      >
        <Input.Password />
      </Form.Item>
      <Button type="primary" htmlType="submit" loading={loading}>
        修改密码
      </Button>
    </Form>
  );
};
``` 