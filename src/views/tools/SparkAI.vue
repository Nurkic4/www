<template>
  <div class="sparkai-bg">
    <div class="sparkai-main">
      <!-- 简化头部区域，重新排列布局 -->
      <div class="sparkai-header">
        <div class="header-left">
            <div class="main-title">{{ t('title') }}</div>
          <!-- 将下拉选项移到标题旁边 -->
          <el-select v-model="mode" class="mode-select" style="width: 120px; margin-left: 15px;">
            <el-option :label="t('direct')" value="direct" />
            <el-option :label="t('side')" value="side" />
            <el-option :label="t('battle')" value="battle" />
          </el-select>
          <!-- 只在 direct/side 下显示模型选择，battle 不显示模型选择 -->
          <el-select v-if="mode === 'direct'" v-model="selectedModel1" class="model-select" style="width: 80px; margin-left: 10px;">
              <el-option
                v-for="model in availableModels"
                :key="model"
              :label="getModelName(model)"
                :value="model"
              />
            </el-select>
          <template v-else-if="mode === 'side'">
            <el-select v-model="selectedModel1" class="model-select" style="width: 80px; margin-left: 10px;">
              <el-option
                v-for="model in availableModels"
                :key="model"
                :label="getModelName(model)"
                :value="model"
              />
            </el-select>
            <span class="vs-text">VS</span>
            <el-select v-model="selectedModel2" class="model-select" style="width: 80px;">
              <el-option
                v-for="model in availableModels"
                :key="model"
                :label="getModelName(model)"
                :value="model"
              />
            </el-select>
          </template>
        </div>
        <div class="header-right">
          <button @click="lang = !lang" class="lang-btn">
            {{ lang ? 'EN' : '中文' }}
          </button>
          <button class="debug-btn" @click="debugMode = !debugMode" :class="{ active: debugMode }">
            🔧
          </button>
          <button class="new-chat-btn" @click="newChat">➕</button>
        </div>
      </div>
      <div v-if="testing" class="auto-test-tip">
        <span class="loading-spinner"></span>
        {{ t('testingTip') }}
      </div>
      <div v-if="debugMode" class="debug-panel">
        <div class="debug-title">🔧 {{ t('debugMode') }}</div>
        <div class="debug-info">
          <div><strong>API URL:</strong> {{ apiUrl }}</div>
          <div><strong>{{ t('currentModel') }}</strong> {{ selectedModel1 }}</div>
          <div><strong>{{ t('language') }}</strong> {{ lang ? 'EN' : 'CN' }}</div>
          <div><strong>{{ t('mode') }}</strong> {{ mode }}</div>
        </div>
        <div class="debug-actions">
          <button class="test-btn" @click="testAllModels" :disabled="testing">🔍 {{ t('testModels') }}</button>
          <button class="test-btn" @click="checkApiServer" :disabled="testing">🌐 {{ t('checkServer') }}</button>
          <button class="test-btn" @click="resetTesting" style="background: #ff6b6b;">🔄 重置状态</button>
        </div>
      </div>
      <div v-if="mode === 'direct'">
        <div class="sparkai-chat-area" ref="historyRef">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            :class="['chat-msg', msg.role]"
          >
            <div class="avatar-wrap">
              <img
                v-if="msg.role === 'user'"
                class="avatar"
                :src="userSvg"
                alt="用户头像"
              />
              <img
                v-else
                class="avatar"
                :src="aiLogo1"
                alt="AI头像"
              />
            </div>
            <div class="bubble" :class="{'error-bubble': msg.content && (msg.content.includes(t('errorPrefix')) || msg.content.includes(t('networkError')) || msg.content.includes(t('error10003')))}">
              <span class="msg-content">
                <template v-if="msg.loading">
                  <span class="ai-loading-spinner"></span>
                </template>
                <template v-else>
                  {{ msg.content }}
                  <button 
                    v-if="msg.role === 'assistant' && (msg.content.includes(t('errorPrefix')) || msg.content.includes(t('networkError')) || msg.content.includes(t('error10003')))" 
                    class="retry-btn" 
                    @click="retryLastMessage"
                  >
                    🔄 {{ t('retry') }}
                  </button>
                </template>
              </span>
            </div>
          </div>
        </div>
        <div class="sparkai-input-bar">
          <textarea
            v-model="input"
            class="chat-input"
            :placeholder="t('inputPlaceholder')"
            @keydown.enter.exact.prevent="sendMsg"
            :disabled="loading"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendMsg"
            :disabled="loading || !input.trim()"
          >
            <span v-if="!loading">{{ t('send') }}</span>
            <span v-else>{{ t('sending') }}</span>
          </button>
        </div>
      </div>
      <div v-else-if="mode === 'side'">
        <div class="side-by-side-chat-area">
          <div v-for="(item, idx) in sideBySideMessages" :key="idx" class="side-by-side-row">
            <div class="side-col user-col">
              <div class="side-model-label">{{ t('userQuestion') }}</div>
              <div class="side-question">{{ item.question }}</div>
            </div>
            <div class="side-col model1-col">
              <div class="side-model-label">{{ getModelName(selectedModel1) }}</div>
              <div class="side-bubble" :class="{'error-bubble': item.reply1 && (item.reply1.includes(t('errorPrefix')) || item.reply1.includes(t('networkError')) || item.reply1.includes(t('error10003')))}">
                <template v-if="item.loading1">
                  <span class="ai-loading-spinner"></span>
                </template>
                <template v-else>
                  {{ item.reply1 }}
                </template>
              </div>
            </div>
            <div class="side-col model2-col">
              <div class="side-model-label">{{ getModelName(selectedModel2) }}</div>
              <div class="side-bubble" :class="{'error-bubble': item.reply2 && (item.reply2.includes(t('errorPrefix')) || item.reply2.includes(t('networkError')) || item.reply2.includes(t('error10003')))}">
                <template v-if="item.loading2">
                  <span class="ai-loading-spinner"></span>
                </template>
                <template v-else>
                  {{ item.reply2 }}
                </template>
              </div>
            </div>
            <button 
              v-if="(item.reply1 && (item.reply1.includes(t('errorPrefix')) || item.reply1.includes(t('networkError')) || item.reply1.includes(t('error10003')))) || 
                    (item.reply2 && (item.reply2.includes(t('errorPrefix')) || item.reply2.includes(t('networkError')) || item.reply2.includes(t('error10003'))))" 
              class="side-retry-btn" 
              @click="retryLastSideBySide"
            >
              🔄 {{ t('retry') }}
            </button>
          </div>
        </div>
        <div class="sparkai-input-bar">
          <textarea
            v-model="sideInput"
            class="chat-input"
            :placeholder="t('inputPlaceholder')"
            @keydown.enter.exact.prevent="sendSideBySide"
            :disabled="loading"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendSideBySide"
            :disabled="loading || !sideInput.trim()"
          >
            <span v-if="!loading">{{ t('send') }}</span>
            <span v-else>{{ t('sending') }}</span>
          </button>
        </div>
      </div>
      <div v-else-if="mode === 'battle'">
        <div class="battle-chat-area">
          <div v-for="(item, idx) in battleMessages" :key="idx" class="battle-row">
            <div class="battle-question">
              <span class="side-model-label">{{ t('userQuestion') }}</span>
              <span class="side-question">{{ item.question }}</span>
            </div>
            <div class="battle-replies">
              <div v-for="reply in item.replies" :key="reply.model" class="battle-reply-row">
                <div class="side-model-label">{{ getModelName(reply.model) }}</div>
                <div class="side-bubble" :class="{'error-bubble': reply.reply && (reply.reply.includes(t('errorPrefix')) || reply.reply.includes(t('networkError')) || reply.reply.includes(t('error10003')))}">
                  <template v-if="reply.loading">
                    <span class="ai-loading-spinner"></span>
                  </template>
                  <template v-else>
                    {{ reply.reply }}
                  </template>
                </div>
              </div>
            </div>
            <button v-if="item.replies.some(r => r.reply && (r.reply.includes(t('errorPrefix')) || r.reply.includes(t('networkError')) || r.reply.includes(t('error10003'))))" class="side-retry-btn" @click="retryLastBattle">
              🔄 {{ t('retry') }}
            </button>
          </div>
        </div>
        <div class="sparkai-input-bar">
          <textarea
            v-model="battleInput"
            class="chat-input"
            :placeholder="t('inputPlaceholder')"
            @keydown.enter.exact.prevent="sendBattle"
            :disabled="battleLoading"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendBattle"
            :disabled="battleLoading || !battleInput.trim()"
          >
            <span v-if="!battleLoading">{{ t('send') }}</span>
            <span v-else>{{ t('sending') }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { ElSelect, ElOption, ElSwitch } from 'element-plus';
import 'element-plus/es/components/select/style/css';
import 'element-plus/es/components/option/style/css';
import 'element-plus/es/components/switch/style/css';
import { request } from '@/utils/request.ts';
// 新增图片import
import aiLogo1 from '@/views/css/svg/AI-logo1.svg';
import aiLogo2 from '@/views/css/svg/AI-logo2.svg';
import userSvg from '@/views/css/svg/user.svg';

// 国际化相关，确保 t 是顶层变量
const lang = ref(false); // false=中文，true=英文
const t = (key) => lang.value ? en[key] : zh[key];

// 在<script setup>部分添加调试模式变量
const debugMode = ref(false); // 调试模式开关

// 在zh和en对象中添加调试相关的文本
const zh = {
  direct: '单聊模式',
  side: '对比模式',
  battle: '混战模式', // 新增
  newChat: '新建对话',
  title: '星火 AI 聊天',
  subtitle: '多模型对话体验 · X1 / Lite / Max / Pro / Ultra',
  testModels: '测试模型',
  testingTip: '正在自动检测模型状态，请稍候...',
  inputPlaceholder: '请输入你的问题，按Enter发送...',
  send: '发送',
  sending: '发送中...',
  userQuestion: '用户提问',
  error10003: '星火AI暂时无法响应，请稍后再试或更换模型。(错误码: 10003)',
  networkError: '网络请求失败',
  parseError: '响应解析失败',
  retry: '重试',
  debugMode: '调试模式',
  serverError: '服务器错误 (500)',
  toggleDebug: '切换调试',
  checkServer: '检查服务器',
  checkingServer: '正在检查API服务器状态...',
  serverOk: '✅ API服务器正常 (响应时间: {0}ms)',
  serverError500: '❌ API服务器返回错误状态码: {0} {1}',
  serverInfo: '服务器信息:',
  noServerInfo: '无法获取服务器信息',
  testRequestOk: '✅ 测试请求成功',
  testRequestFail: '❌ 测试请求失败: {0} {1}',
  testRequestError: '❌ 测试请求异常: {0}',
  serverStatusResult: '🌐 API服务器状态检查结果:',
  cannotConnectServer: '❌ 无法连接到API服务器: {0}',
  sparkApiError: '星火API错误: {0} ({1})',
  unknownErrorCode: '未知错误码',
  errorPrefix: '错误: ',
  debugInfo: '调试信息: ',
  parseErrorWithMsg: '解析错误: ',
  modelAvailable: '✅ 可用',
  modelDiagnosisResult: '🔍 模型诊断结果:',
  noResponse: 'AI无回复',
  currentModel: '当前模型:',
  language: '语言:',
  mode: '模式:'
};
const en = {
  direct: 'Direct Chat',
  side: 'Side by Side',
  battle: 'Battle', // 新增
  newChat: 'New Chat',
  title: 'Spark AI Chat',
  subtitle: 'Multi-model Dialogue · X1 / Lite / Max / Pro / Ultra',
  testModels: 'Test Models',
  testingTip: 'Testing model status, please wait...',
  inputPlaceholder: 'Type your question and press Enter...',
  send: 'Send',
  sending: 'Sending...',
  userQuestion: 'User Question',
  error10003: 'Spark AI is temporarily unavailable. Please try again later or switch models. (Error code: 10003)',
  networkError: 'Network request failed',
  parseError: 'Failed to parse response',
  retry: 'Retry',
  debugMode: 'Debug Mode',
  serverError: 'Server Error (500)',
  toggleDebug: 'Toggle Debug',
  checkServer: 'Check Server',
  checkingServer: 'Checking API server status...',
  serverOk: '✅ API server is working properly (Response time: {0}ms)',
  serverError500: '❌ API server returned error status: {0} {1}',
  serverInfo: 'Server Information:',
  noServerInfo: 'Could not retrieve server information',
  testRequestOk: '✅ Test request successful',
  testRequestFail: '❌ Test request failed: {0} {1}',
  testRequestError: '❌ Test request error: {0}',
  serverStatusResult: '🌐 API Server Status Check Results:',
  cannotConnectServer: '❌ Cannot connect to API server: {0}',
  sparkApiError: 'Spark API Error: {0} ({1})',
  unknownErrorCode: 'Unknown error code',
  errorPrefix: 'Error: ',
  debugInfo: 'Debug info: ',
  parseErrorWithMsg: 'Parse error: ',
  modelAvailable: '✅ Available',
  modelDiagnosisResult: '🔍 Model Diagnosis Results:',
  noResponse: 'No AI response',
  currentModel: 'Current Model:',
  language: 'Language:',
  mode: 'Mode:'
};

// 辅助函数：检查是否是10003错误
function is10003Error(text) {
  if (!text) return false;
  
  // 直接检查文本是否包含10003
  if (typeof text === 'string' && text.includes('10003')) {
    return true;
  }
  
  // 尝试解析JSON
  try {
    let data = JSON.parse(text);
    
    // 检查第一层error
    if (data.error) {
      // 如果error是字符串
      if (typeof data.error === 'string') {
        if (data.error.includes('10003')) {
          return true;
        }
        
        // 尝试解析嵌套JSON
        if (data.error.includes('{')) {
          try {
            let nestedError = JSON.parse(data.error);
            if (nestedError.error && nestedError.error.code === '10003') {
              return true;
            }
          } catch (e) {
            // 解析失败，继续检查
          }
        }
      }
      // 如果error是对象
      else if (typeof data.error === 'object') {
        if (data.error.code === '10003') {
          return true;
        }
      }
    }
  } catch (e) {
    // JSON解析失败，不是10003错误
  }
  
  return false;
}

// 修改parseErrorResponse函数，使用is10003Error辅助函数
function parseErrorResponse(res, text) {
  if (!res.ok) {
    // 解析错误信息
    let errorMsg = `${t('networkError')} (${res.status}): ${res.statusText}`;
    
    // 检查是否是10003错误
    if (is10003Error(text)) {
      return t('error10003');
    }
    
    try {
      // 尝试解析第一层JSON
      const errorData = JSON.parse(text);
      if (errorData.error) {
        // 检查是否是字符串形式的嵌套JSON
        if (typeof errorData.error === 'string' && errorData.error.includes('{')) {
          try {
            // 尝试解析嵌套的JSON
            const nestedError = JSON.parse(errorData.error);
            if (nestedError.error && nestedError.error.message) {
              errorMsg = t('sparkApiError').replace('{0}', nestedError.error.message).replace('{1}', nestedError.error.code || t('unknownErrorCode'));
            } else {
              errorMsg = errorData.error;
            }
          } catch (e) {
            // 如果嵌套JSON解析失败，使用原始错误
            errorMsg = errorData.error;
          }
        } else {
          errorMsg = errorData.error;
        }
      }
    } catch (e) {
      // 如果无法解析JSON，使用原始文本
      errorMsg = text || errorMsg;
    }
    return errorMsg;
  } else {
    // 处理成功响应
    try {
      const data = JSON.parse(text);
      if (data.error) {
        // 检查是否是10003错误
        if (is10003Error(data.error)) {
          return t('error10003');
        }
        return t('errorPrefix') + data.error;
      }
      
      // 提取AI回复内容
      if (data.choices && data.choices.length > 0) {
        return data.choices[0].message?.content || t('noResponse');
      } else if (data.data && data.data.length > 0) {
        return data.data[0].content || t('noResponse');
      } else if (data.content) {
        return data.content;
      } else {
        return t('noResponse');
      }
    } catch (e) {
      return t('parseError') + ': ' + text;
    }
  }
}

// ========== 新增：补全所有响应式变量 ==========
const mode = ref('direct'); // 聊天模式，默认单聊
const selectedModel1 = ref('x1'); // 默认模型1
const selectedModel2 = ref('lite'); // 默认模型2
const testing = ref(false); // 是否正在测试模型
const availableModels = ref(['x1', 'lite', 'max', 'pro', 'ultra']); // 可用模型列表
const messages = ref([]); // 单聊消息列表
const input = ref(''); // 单聊输入框
const loading = ref(false); // 发送中
const sideBySideMessages = ref([]); // 对比模式消息
const sideInput = ref(''); // 对比模式输入框
const modelStatus = ref({});
const battleMessages = ref([]); // Battle 模式消息
const battleInput = ref('');
const battleLoading = ref(false);

// ========== 添加：主题切换监听 ==========
// 监听文档上的data-theme属性变化
const observer = new MutationObserver((mutations) => {
  mutations.forEach((mutation) => {
    if (mutation.attributeName === 'data-theme') {
      // 当data-theme属性变化时，强制重新渲染组件
      nextTick(() => {
        const container = document.querySelector('.sparkai-main');
        if (container) {
          container.style.opacity = '0.99';
          setTimeout(() => {
            container.style.opacity = '1';
          }, 10);
        }
        // 更新错误样式变量
        updateErrorStyleVars();
      });
    }
  });
});

// 更新错误样式变量和背景
function updateErrorStyleVars() {
  const root = document.documentElement;
  const theme = root.getAttribute('data-theme') || 'light';
  
  if (theme === 'dark') {
    root.style.setProperty('--error-color', '#5a2530');
    root.style.setProperty('--error-border-color', '#ff6b6b');
    root.style.setProperty('--error-text-color', '#ffb4b4');
    
    // 强制设置SparkAI背景为深色
    const sparkaiContainer = document.querySelector('.sparkai-bg');
    if (sparkaiContainer) {
      sparkaiContainer.style.backgroundColor = '#1e1f30';
    }
  } else {
    root.style.setProperty('--error-color', '#fff0f0');
    root.style.setProperty('--error-border-color', '#ffcaca');
    root.style.setProperty('--error-text-color', '#d32f2f');
    
    // 强制设置SparkAI背景为浅色
    const sparkaiContainer = document.querySelector('.sparkai-bg');
    if (sparkaiContainer) {
      sparkaiContainer.style.backgroundColor = '#f8fafc';
    }
  }
}

// 在组件卸载时停止监听
onUnmounted(() => {
  observer.disconnect();
});

// ========== 新增：模型名称映射 ==========
const modelNameMap = {
  x1: 'X1',
  lite: 'Lite',
  max: 'Max',
  pro: 'Pro',
  ultra: 'Ultra'
};
function getModelName(model) {
  return modelNameMap[model] || model;
}
// ========== 新增：模型状态映射 ==========
const modelStatusMap = {
  x1: '✅',
  lite: '✅',
  max: '✅',
  pro: '✅',
  ultra: '✅'
};
function getModelStatus(model) {
  // 这里可以根据实际业务逻辑返回不同状态
  return modelStatusMap[model] || '';
}
// ========== 新增：补全所有响应式变量 END ==========

const apiUrl = '/api/sparkai';
const historyRef = ref(null);

function newChat() {
  if (mode.value === 'direct') {
    messages.value = [
      { role: 'system', content: '我是星火AI助手，您可以用中文与我对话。' }
    ];
    input.value = '';
    nextTick(() => {
      if (historyRef.value) {
        historyRef.value.scrollTop = 0;
      }
    });
  } else {
    sideBySideMessages.value = [];
    sideInput.value = '';
  }
}

// 修改sendMsg函数，添加更多的错误处理和调试信息
async function sendMsg() {
  if (!input.value.trim()) return;
  // 1. 用户消息
  messages.value.push({ role: 'user', content: input.value });
  // 2. loading气泡
  messages.value.push({ role: 'assistant', content: '', loading: true });
  loading.value = true;
  // 3. 组装请求体（只要user/assistant）
  const reqMsgs = messages.value
    .filter(m => m.role === 'user' || m.role === 'assistant')
    .map(m => ({ role: m.role, content: m.content }));
  
  // 调试模式下，记录请求信息
  if (debugMode.value) {
    console.log('发送请求:', {
      url: apiUrl,
      model: selectedModel1.value,
      messages: reqMsgs
    });
  }
  
  try {
    const { data, error } = await request({ 
      url: apiUrl, 
      method: 'POST', 
      data: {
        model: selectedModel1.value,
        messages: reqMsgs
      },
      timeout: 60000 // 为AI请求设置60秒超时
    });
    
    // 调试模式下，记录响应状态
    if (debugMode.value) {
      console.log('响应状态:', data?.status, data?.statusText);
      console.log('响应数据:', data);
      console.log('响应内容:', data?.data || '');
      console.log('错误对象:', error);
    }
    
    // 简化响应处理逻辑
    if (error) {
      // 有错误对象，显示错误信息
      let errorMsg = '';
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        errorMsg = `请求超时 (60秒)，请检查网络连接或稍后重试。`;
      } else if (error.message.includes('Network Error')) {
        errorMsg = `网络连接失败，请检查网络设置。`;
      } else {
        errorMsg = `${t('networkError')}: ${error.message}`;
      }
      
      if (debugMode.value) {
        errorMsg += `\n\n${t('debugInfo')}${error.stack || ''}`;
      }
      
      messages.value[messages.value.length-1] = { role: 'assistant', content: errorMsg };
      loading.value = false;
      return;
    }
    
    // 没有错误对象，尝试处理响应数据
    let parsedData = data;
    if (typeof parsedData === 'string') {
      try {
        parsedData = JSON.parse(parsedData);
      } catch (e) {
        messages.value[messages.value.length-1] = { 
          role: 'assistant', 
          content: `${t('parseError')}: ${parsedData}${debugMode.value ? '\n\n' + t('parseErrorWithMsg') + e.message : ''}` 
        };
        loading.value = false;
        return;
      }
    }
    if (debugMode.value) {
      console.log('parsedData:', parsedData);
      if (parsedData && parsedData.choices) {
        console.log('choices:', parsedData.choices);
        console.log('message:', parsedData.choices[0]?.message);
      }
    }
    // 检查解析后的数据是否有错误
    if (parsedData && parsedData.error) {
      if (is10003Error(parsedData.error)) {
        messages.value[messages.value.length-1] = { role: 'assistant', content: t('error10003') };
      } else {
        messages.value[messages.value.length-1] = { 
          role: 'assistant', 
          content: t('errorPrefix') + parsedData.error + (debugMode.value ? '\n\n' + t('debugInfo') + JSON.stringify(parsedData, null, 2) : '') 
        };
      }
      loading.value = false;
      return;
    }
    // ======= 兜底提取AI回复内容 =======
    let aiContent = '';
    try {
      let root = parsedData;
      if (root && typeof root === 'object' && 'data' in root && !('choices' in root)) {
        root = root.data;
      }
      if (root && root.choices && root.choices.length > 0) {
        const msg = root.choices[0].message;
        aiContent = (msg && (msg.reasoning_content || msg.content)) || '';
      } else if (root && root.data && Array.isArray(root.data) && root.data.length > 0) {
        aiContent = root.data[0].content || '';
      } else if (root && root.content) {
        aiContent = root.content;
      } else {
        aiContent = JSON.stringify(root);
      }
    } catch (e) {
      aiContent = t('parseError') + ': ' + e.message;
    }
    if (!aiContent) aiContent = t('noResponse');
    if (debugMode.value) {
      console.log('最终提取的AI内容:', aiContent);
    }
    messages.value[messages.value.length-1] = { role: 'assistant', content: aiContent };
    input.value = '';
    nextTick(() => {
      if (historyRef.value) historyRef.value.scrollTop = historyRef.value.scrollHeight;
    });
  } catch (e) {
    // 更详细的错误处理
    let errorMsg = '';
    if (e.code === 'ECONNABORTED' || e.message.includes('timeout')) {
      errorMsg = `请求超时 (60秒)，请检查网络连接或稍后重试。${debugMode.value ? '\n\n' + t('debugInfo') + e.stack : ''}`;
    } else if (e.message.includes('Network Error')) {
      errorMsg = `网络连接失败，请检查网络设置。${debugMode.value ? '\n\n' + t('debugInfo') + e.stack : ''}`;
    } else {
      errorMsg = `${t('networkError')}: ${e.message}${debugMode.value ? '\n\n' + t('debugInfo') + e.stack : ''}`;
    }
    
    messages.value[messages.value.length-1] = { 
      role: 'assistant', 
      content: errorMsg
    };
  } finally {
    loading.value = false;
  }
}

async function sendSideBySide() {
  if (!sideInput.value.trim()) return;
  loading.value = true;
  const question = sideInput.value;
  // 先插入loading气泡
  sideBySideMessages.value.push({ question, reply1: '', reply2: '', loading1: true, loading2: true });
  const idx = sideBySideMessages.value.length - 1;
  try {
    const reqBody1 = {
      model: selectedModel1.value,
      messages: [{ role: 'user', content: question }]
    };
    const reqBody2 = {
      model: selectedModel2.value,
      messages: [{ role: 'user', content: question }]
    };
    // 并发请求，添加超时设置
    const [res1, res2] = await Promise.all([
      request({ 
        url: apiUrl, 
        method: 'POST', 
        data: reqBody1,
        timeout: 60000
      }),
      request({ 
        url: apiUrl, 
        method: 'POST', 
        data: reqBody2,
        timeout: 60000
      })
    ]);
    
    // 处理响应
    let reply1, reply2;
    // 处理第一个响应
    if (res1.error) {
      reply1 = `网络请求失败: ${res1.error.message}`;
    } else {
      let parsed1 = res1.data;
      if (typeof parsed1 === 'string') {
        try { parsed1 = JSON.parse(parsed1); } catch (e) { reply1 = `${t('parseError')}: ${parsed1}`; }
      }
      if (!reply1) {
        // ======= 兜底提取AI回复内容 =======
        try {
          let root = parsed1;
          if (root && typeof root === 'object' && 'data' in root && !('choices' in root)) {
            root = root.data;
          }
          if (root && root.choices && root.choices.length > 0) {
            const msg = root.choices[0].message;
            reply1 = (msg && (msg.reasoning_content || msg.content)) || '';
          } else if (root && root.data && Array.isArray(root.data) && root.data.length > 0) {
            reply1 = root.data[0].content || '';
          } else if (root && root.content) {
            reply1 = root.content;
          } else {
            reply1 = JSON.stringify(root);
          }
        } catch (e) {
          reply1 = t('parseError') + ': ' + e.message;
        }
        if (!reply1) reply1 = t('noResponse');
      }
    }
    // 处理第二个响应
    if (res2.error) {
      reply2 = `网络请求失败: ${res2.error.message}`;
    } else {
      let parsed2 = res2.data;
      if (typeof parsed2 === 'string') {
        try { parsed2 = JSON.parse(parsed2); } catch (e) { reply2 = `${t('parseError')}: ${parsed2}`; }
      }
      if (!reply2) {
        // ======= 兜底提取AI回复内容 =======
        try {
          let root = parsed2;
          if (root && typeof root === 'object' && 'data' in root && !('choices' in root)) {
            root = root.data;
          }
          if (root && root.choices && root.choices.length > 0) {
            const msg = root.choices[0].message;
            reply2 = (msg && (msg.reasoning_content || msg.content)) || '';
          } else if (root && root.data && Array.isArray(root.data) && root.data.length > 0) {
            reply2 = root.data[0].content || '';
          } else if (root && root.content) {
            reply2 = root.content;
          } else {
            reply2 = JSON.stringify(root);
          }
        } catch (e) {
          reply2 = t('parseError') + ': ' + e.message;
        }
        if (!reply2) reply2 = t('noResponse');
      }
    }
    
    if (debugMode.value) {
      console.log('对比模式回复1:', reply1);
      console.log('对比模式回复2:', reply2);
    }
    
    sideBySideMessages.value[idx] = { question, reply1, reply2, loading1: false, loading2: false };
    sideInput.value = '';
    nextTick(() => {
      const area = document.querySelector('.side-by-side-chat-area');
      if (area) area.scrollTop = area.scrollHeight;
    });
  } catch (e) {
    let errorMsg = '';
    if (e.code === 'ECONNABORTED' || e.message.includes('timeout')) {
      errorMsg = '请求超时 (60秒)，请检查网络连接或稍后重试。';
    } else if (e.message.includes('Network Error')) {
      errorMsg = '网络连接失败，请检查网络设置。';
    } else {
      errorMsg = '网络请求失败: ' + e.message;
    }
    
    sideBySideMessages.value[idx] = {
      question,
      reply1: errorMsg,
      reply2: errorMsg,
      loading1: false,
      loading2: false
    };
  } finally {
    loading.value = false;
  }
}

// 修正后的 testAllModels
async function testAllModels() {
  testing.value = true;
  const models = ['x1', 'lite', 'max', 'pro', 'ultra'];
  const results = {};
  for (const model of models) {
    try {
      const res = await request({ 
        url: '/api/sparkai', 
        method: 'POST', 
        data: {
          model,
          messages: [{ role: 'user', content: 'ping' }]
        },
        timeout: 30000 // 测试请求30秒超时
      });
      const text = res.data || '';
      if (!res.ok) {
        results[model] = { status: 'error', message: `${t('networkError')} (${res.status}): ${text || res.statusText}` };
      } else {
        let parsedData;
        try { parsedData = JSON.parse(text); } catch { parsedData = {}; }
        if (parsedData.error) {
          results[model] = { status: 'error', message: t('errorPrefix') + parsedData.error };
        } else {
          results[model] = { status: 'success', message: t('modelAvailable') };
        }
      }
    } catch (e) {
      let errorMsg = '';
      if (e.code === 'ECONNABORTED' || e.message.includes('timeout')) {
        errorMsg = '请求超时 (30秒)';
      } else if (e.message.includes('Network Error')) {
        errorMsg = '网络连接失败';
      } else {
        errorMsg = t('networkError') + ': ' + e.message;
      }
      results[model] = { status: 'error', message: errorMsg };
    }
    
    // 添加延迟避免请求过于频繁
    if (model !== models[models.length - 1]) {
      await new Promise(r => setTimeout(r, 200));
    }
  }
  modelStatus.value = results;
  testing.value = false;

  // 输出测试结果到聊天记录
  let resultText = t('modelDiagnosisResult') + '\n';
  for (const [model, result] of Object.entries(results)) {
    resultText += `${getModelName(model)}: ${result.message}\n`;
  }
  messages.value.push({ role: 'system', content: resultText });
}

// 添加检查API服务器状态的函数
async function checkApiServer() {
  testing.value = true;
  messages.value.push({ role: 'system', content: t('checkingServer') });
  
  try {
    // 尝试发送一个简单的OPTIONS请求
    const startTime = Date.now();
    const res = await request({ url: apiUrl, method: 'OPTIONS' });
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    let status = '';
    if (res.ok) {
      status = t('serverOk').replace('{0}', responseTime);
    } else {
      status = t('serverError500').replace('{0}', res.status).replace('{1}', res.statusText);
    }
    
    // 尝试获取服务器信息
    let serverInfo = '';
    try {
      const headers = {};
      res.headers.forEach((value, key) => {
        headers[key] = value;
      });
      serverInfo = `\n\n${t('serverInfo')}\n${JSON.stringify(headers, null, 2)}`;
    } catch (e) {
      serverInfo = `\n\n${t('noServerInfo')}`;
    }
    
    // 尝试发送一个简单的测试请求
    try {
      const testRes = await request({ url: apiUrl, method: 'POST', data: {
        model: 'x1',
        messages: [{ role: 'user', content: 'ping' }]
      } });
      
      const testText = testRes.data || '';
      let testStatus = '';
      
      if (testRes.ok) {
        testStatus = `\n${t('testRequestOk')}`;
      } else {
        testStatus = `\n${t('testRequestFail').replace('{0}', testRes.status).replace('{1}', testRes.statusText)}`;
      }
      
      if (debugMode.value) {
        testStatus += `\n${testText.substring(0, 200)}${testText.length > 200 ? '...' : ''}`;
      }
      
      status += testStatus;
    } catch (e) {
      status += `\n${t('testRequestError').replace('{0}', e.message)}`;
    }
    
    messages.value[messages.value.length-1] = { 
      role: 'system', 
      content: `${t('serverStatusResult')}\n${status}${debugMode.value ? serverInfo : ''}` 
    };
  } catch (e) {
    messages.value[messages.value.length-1] = { 
      role: 'system', 
      content: `${t('cannotConnectServer').replace('{0}', e.message)}${debugMode.value ? '\n\n' + e.stack : ''}` 
    };
  } finally {
    testing.value = false;
    nextTick(() => {
      if (historyRef.value) historyRef.value.scrollTop = historyRef.value.scrollHeight;
    });
  }
}

// 在<script setup>部分添加重试函数
function retryLastMessage() {
  if (messages.value.length < 2) return;
  // 移除上一条AI回复
  messages.value.pop();
  // 重新发送
  sendMsg();
}

function retryLastSideBySide() {
  if (sideBySideMessages.value.length === 0) return;
  // 获取最后一条问题
  const lastQuestion = sideBySideMessages.value[sideBySideMessages.value.length - 1].question;
  // 移除最后一条记录
  sideBySideMessages.value.pop();
  // 设置输入并重新发送
  sideInput.value = lastQuestion;
  sendSideBySide();
}

// Battle 模式发送函数（优化：顺序请求+自动重试+loading动画）
async function sendBattle() {
  if (!battleInput.value.trim()) return;
  battleLoading.value = true;
  const question = battleInput.value;
  const models = ['x1', 'lite', 'max', 'pro', 'ultra'];
  // 先插入loading气泡
  const replies = models.map(m => ({ model: m, reply: '', loading: true }));
  battleMessages.value.push({ question, replies });
  const idx = battleMessages.value.length - 1;
  for (let i = 0; i < models.length; i++) {
    const model = models[i];
    let reply = '';
    let success = false;
    let attempts = 0;
    while (!success && attempts < 3) { // 最多重试2次
      attempts++;
      try {
        const res = await request({ url: apiUrl, method: 'POST', data: {
          model,
          messages: [{ role: 'user', content: question }]
        } });
        const text = res.data || '';
        // 优先reasoning_content
        let parsed = text;
        if (typeof parsed === 'string') {
          try { parsed = JSON.parse(parsed); } catch (e) { parsed = {}; }
        }
        // ======= 兜底提取AI回复内容 =======
        try {
          let root = parsed;
          if (root && typeof root === 'object' && 'data' in root && !('choices' in root)) {
            root = root.data;
          }
          if (root && root.choices && root.choices.length > 0) {
            const msg = root.choices[0].message;
            reply = (msg && (msg.reasoning_content || msg.content)) || '';
          } else if (root && root.data && Array.isArray(root.data) && root.data.length > 0) {
            reply = root.data[0].content || '';
          } else if (root && root.content) {
            reply = root.content;
          } else {
            reply = JSON.stringify(root);
          }
        } catch (e) {
          reply = t('parseError') + ': ' + e.message;
        }
        if (!reply) reply = t('noResponse');
        if (res.status === 403 || (typeof reply === 'string' && reply.includes('授权错误'))) {
          if (attempts < 3) {
            await new Promise(r => setTimeout(r, 400));
            continue;
          }
        }
        success = true;
      } catch (e) {
        reply = '网络请求失败: ' + e.message;
        success = true;
      }
    }
    // 更新对应模型的loading和内容
    battleMessages.value[idx].replies[i] = { model, reply, loading: false };
    if (i < models.length - 1) await new Promise(r => setTimeout(r, 200));
  }
  battleInput.value = '';
  battleLoading.value = false;
  nextTick(() => {
    const area = document.querySelector('.battle-chat-area');
    if (area) area.scrollTop = area.scrollHeight;
  });
}

function retryLastBattle() {
  if (battleMessages.value.length === 0) return;
  const last = battleMessages.value[battleMessages.value.length - 1];
  battleMessages.value.pop();
  battleInput.value = last.question;
  sendBattle();
}

// 添加重置testing状态的函数
function resetTesting() {
  testing.value = false;
  loading.value = false;
  battleLoading.value = false;
  console.log('已重置所有loading状态');
}

// 监听模式变化，切换输入框
watch(mode, (newVal) => {
  if (newVal === 'direct') {
    input.value = ''; // 清空单聊输入框
    nextTick(() => {
      if (historyRef.value) {
        historyRef.value.scrollTop = 0;
      }
    });
  } else if (newVal === 'side') {
    sideInput.value = ''; // 清空对比模式输入框
  } else if (newVal === 'battle') {
    battleInput.value = ''; // 清空混战模式输入框
  }
});

// 初始化时测试所有模型
onMounted(() => {
  // 开始监听文档上的data-theme属性变化
  observer.observe(document.documentElement, { attributes: true });
  // 确保testing状态为false
  testing.value = false;
  // 移除自动测试模型
  // testAllModels();
  // 初始化错误样式变量
  updateErrorStyleVars();
  
  // 立即应用当前主题的背景色
  const theme = document.documentElement.getAttribute('data-theme') || 'light';
  const sparkaiContainer = document.querySelector('.sparkai-bg');
  if (sparkaiContainer) {
    sparkaiContainer.style.backgroundColor = theme === 'dark' ? '#1e1f30' : '#f8fafc';
  }
});
</script>

<style>
.sparkai-bg {
  min-height: 100vh;
  display: flex;
  align-items: flex-start; /* 改为顶部对齐 */
  justify-content: center;
  padding: 0;
  margin: 0;
  width: 100%;
  background-color: var(--ai-bg);
  color: var(--ai-text);
  position: absolute; /* 改为绝对定位，而不是固定定位 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto; /* 允许垂直滚动 */
  z-index: 0; /* 确保背景在底层 */
}

.sparkai-main {
  width: 100%;
  max-width: 100%;
  min-height: 100vh; /* 改回最小高度 */
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  position: relative;
  padding: 120px 0 100px 0; /* 60px主header + 60px sparkai-header，实际高度可微调 */
  margin: 0;
  color: var(--ai-text);
  transition: all 0.3s ease;
  overflow: visible; /* 允许内容溢出 */
  z-index: 1; /* 确保内容在背景之上 */
}

.sparkai-header {
  position: fixed; /* 固定定位 */
  top: 60px; /* 主header高度，必要时调整 */
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid var(--ai-border);
  background: var(--header-bg);
  backdrop-filter: blur(10px);
  border-radius: 0;
  flex-wrap: wrap;
  gap: 5px;
  width: 100%;
  box-sizing: border-box;
  margin: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 10px;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.mode-select, .model-select {
  min-width: auto;
  border-radius: 8px;
  border: 1px solid var(--ai-border);
  font-size: 0.9rem;
  padding: 5px 8px;
  background: var(--card-bg);
  box-shadow: var(--header-shadow);
  outline: none;
  transition: all 0.2s;
  font-weight: 500;
  text-align: center;
  color: var(--ai-text);
  cursor: pointer;
  margin-right: 0;
}

.mode-select:hover {
  background: var(--bg-color);
  border-color: var(--ai-title);
}

.mode-select:focus {
  border: 2px solid var(--ai-title);
  background: var(--card-bg);
  box-shadow: 0 0 0 3px rgba(78,107,222,0.2);
}

.mode-select option {
  padding: 8px 12px;
  font-size: 1rem;
  background: var(--card-bg);
  color: var(--ai-text);
  border: none;
  font-weight: 500;
}

.mode-select option:checked {
  background: var(--ai-title);
  color: #fff;
  font-weight: 600;
}

.mode-select option:hover {
  background: var(--bg-color);
  color: var(--ai-text);
}

.mode-select,
.mode-select:not(:focus),
.mode-select:not(:hover) {
  color: var(--ai-text) !important;
}

.new-chat-btn {
  background: var(--ai-bubble-user);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  padding: 6px 10px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--header-shadow);
  margin-left: 5px;
}

.new-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  filter: brightness(1.1);
}

.ai-logo {
  width: 56px;
  height: 56px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
  transition: transform 0.3s;
}

.ai-logo:hover {
  transform: scale(1.1) rotate(5deg);
}

.main-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--ai-title);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

.sub-title {
  font-size: 1.1rem;
  color: var(--ai-subtitle);
  margin-top: 4px;
  font-weight: 500;
}

.model-select:hover {
  background: var(--bg-color);
  border-color: var(--ai-title);
}

.model-select:focus {
  border: 1.5px solid var(--ai-title);
  background: var(--card-bg);
  box-shadow: 0 0 0 3px rgba(78,107,222,0.2);
}

.model-select option {
  padding: 8px 12px;
  font-size: 1rem;
  background: var(--card-bg);
  color: var(--ai-text);
  border: none;
  font-weight: 500;
}

.model-select option:checked {
  background: var(--ai-title);
  color: #fff;
  font-weight: 600;
}

.model-select option:hover {
  background: var(--bg-color);
  color: var(--ai-text);
}

.model-select,
.model-select:not(:focus),
.model-select:not(:hover) {
  color: var(--ai-text) !important;
}

.sparkai-chat-area {
  flex: 1;
  min-height: 400px; /* 使用最小高度 */
  max-height: none; /* 移除最大高度限制 */
  overflow-y: auto;
  padding: 20px 15px 30px 15px;
  margin-bottom: 80px; /* 为输入框留出空间 */
  scroll-behavior: smooth;
}

.sparkai-chat-area::-webkit-scrollbar {
  width: 6px;
}

.sparkai-chat-area::-webkit-scrollbar-track {
  background: var(--ai-border);
  border-radius: 3px;
}

.sparkai-chat-area::-webkit-scrollbar-thumb {
  background: var(--ai-subtitle);
  border-radius: 3px;
}

.sparkai-chat-area::-webkit-scrollbar-thumb:hover {
  background: var(--ai-title);
}

.chat-msg {
  display: flex;
  margin-bottom: 24px;
  transition: all 0.3s;
  animation: fadeIn 0.4s ease-out;
}

.chat-msg:hover {
  transform: translateX(2px);
}

.chat-msg:last-child {
  margin-bottom: 0;
}

.chat-msg.user {
  flex-direction: row-reverse;
}

.chat-msg.user:hover {
  transform: translateX(-2px);
}

.avatar-wrap {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--card-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: var(--header-shadow);
}

.avatar-wrap:hover {
  transform: scale(1.08) rotate(5deg);
  box-shadow: 0 5px 12px rgba(0,0,0,0.18);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bubble {
  background: var(--ai-bubble);
  border-radius: 18px;
  padding: 12px 18px;
  margin: 0 12px;
  color: var(--ai-text);
  font-size: 1.05rem;
  line-height: 1.5;
  position: relative;
  max-width: 80%;
  word-break: break-word;
  box-shadow: var(--header-shadow);
  backdrop-filter: blur(10px);
  border: 1px solid var(--ai-border);
}

.bubble:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.chat-msg.assistant .bubble {
  border-top-left-radius: 4px;
}

.chat-msg.user .bubble {
  background: var(--ai-bubble-user);
  border-top-right-radius: 4px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--ai-border);
  color: #fff;
}

.chat-msg.user .bubble:hover {
  box-shadow: 0 5px 15px rgba(78,107,222,0.35);
}

.sparkai-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 15px 20px;
  background: var(--card-bg) !important; /* 统一背景色，彻底消除分割线 */
  border-top: 1px solid var(--ai-border);
  border-left: 0;
  border-right: 0;
  border-bottom: 0;
  border-radius: 0;
  z-index: 99;
  box-shadow: none !important; /* 移除阴影 */
}

.chat-input {
  flex: 1;
  min-height: 24px;
  max-height: 120px;
  padding: 12px 18px;
  background: var(--ai-bubble);
  border: 1px solid var(--ai-border);
  border-radius: 12px;
  color: var(--ai-text);
  font-size: 1rem;
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: all 0.2s;
}

.chat-input:focus {
  background: var(--card-bg);
  border-color: var(--ai-title);
  box-shadow: 0 0 0 2px rgba(100, 181, 246, 0.2);
  color: var(--ai-text); /* 保证白天模式聚焦时字体为深色 */
}

.send-btn {
  background: var(--ai-bubble-user);
  color: #fff;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  margin-left: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--header-shadow);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  filter: brightness(1.1);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: var(--ai-border);
  color: var(--ai-subtitle);
  box-shadow: none;
}

.vs-text {
  margin: 0 2px;
  font-size: 0.8rem;
  opacity: 0.7;
}

.side-by-side-chat-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px 15px 30px 15px;
  overflow-y: auto;
  min-height: 400px; /* 使用最小高度 */
  max-height: none; /* 移除最大高度限制 */
  margin-bottom: 80px; /* 为输入框留出空间 */
  scroll-behavior: smooth;
}

.side-by-side-chat-area::-webkit-scrollbar {
  width: 6px;
  background: transparent;
}

.side-by-side-chat-area::-webkit-scrollbar-track {
  background: var(--ai-border);
  border-radius: 3px;
}

.side-by-side-chat-area::-webkit-scrollbar-thumb {
  background: var(--ai-subtitle);
  border-radius: 3px;
}

.side-by-side-chat-area::-webkit-scrollbar-thumb:hover {
  background: var(--ai-title);
}

.side-by-side-row {
  display: flex;
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--header-shadow);
  background: var(--card-bg);
  border: none !important;
  transition: transform 0.3s, box-shadow 0.3s;
  backdrop-filter: blur(10px);
  animation: fadeIn 0.4s ease-out;
}

.side-by-side-row:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.side-col {
  flex: 1;
  padding: 20px;
  border-right: none !important;
  transition: background-color 0.3s;
}

.side-col:hover {
  background-color: var(--bg-color);
}

.user-col {
  background: var(--ai-bubble);
}

.model1-col {
  background: var(--ai-bubble);
}

.model2-col {
  background: var(--ai-bubble);
}

.side-question {
  font-size: 1.1rem;
  line-height: 1.5;
  color: var(--ai-text);
  background: var(--card-bg);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 0;
  border: 1px solid var(--ai-border);
  transition: all 0.2s;
}

.side-question:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--bg-color);
}

.side-model-label {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--ai-title);
  border-bottom: 1px solid var(--ai-border);
  padding-bottom: 8px;
}

.side-bubble {
  background: var(--card-bg);
  color: var(--ai-text);
  border-radius: 12px;
  padding: 16px;
  font-size: 1.1rem;
  line-height: 1.5;
  border: 1px solid var(--ai-border);
  transition: all 0.2s;
  position: relative;
  word-break: break-word;
}

.side-bubble:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--bg-color);
}

.model1-col .side-bubble {
  border-top-left-radius: 4px;
}

.model2-col .side-bubble {
  border-top-left-radius: 4px;
}

.error-bubble {
  background: var(--error-color) !important;
  border: 1px solid var(--error-border-color) !important;
  color: var(--error-text-color) !important;
  box-shadow: 0 3px 12px rgba(255, 0, 0, 0.3) !important;
}

.chat-msg.user .error-bubble {
  background: var(--error-color) !important;
  color: var(--error-text-color) !important;
  border: 1px solid var(--error-border-color) !important;
}

.error-bubble:hover {
  box-shadow: 0 5px 15px rgba(255, 0, 0, 0.4) !important;
}

.retry-btn {
  background: var(--ai-bubble-user);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--header-shadow);
  margin-left: 12px;
}

.retry-btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(78,107,222,0.3);
}

.side-retry-btn {
  background: var(--ai-bubble-user);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--header-shadow);
  margin-top: 16px;
  align-self: center;
}

.side-retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  filter: brightness(1.1);
}

.debug-btn {
  background: var(--card-bg);
  color: var(--ai-text) !important; /* 确保在任何模式下文字颜色都正确 */
  border: 1px solid var(--ai-border);
  border-radius: 8px;
  font-size: 0.9rem;
  padding: 5px 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.debug-btn:hover, .debug-btn.active {
  background: var(--ai-bubble);
  color: var(--ai-title) !important; /* 确保在任何模式下文字颜色都正确 */
  border-color: var(--ai-title);
  transform: translateY(-2px);
}

.debug-panel {
  margin: 16px 20px;
  padding: 16px 20px;
  background: var(--card-bg);
  border-radius: 16px;
  border: 2px solid var(--ai-title);
  box-shadow: var(--header-shadow);
  animation: fadeIn 0.3s;
}

.debug-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--ai-title);
  margin-bottom: 12px;
  border-bottom: 1px solid var(--ai-border);
  padding-bottom: 8px;
}

.debug-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.debug-info div {
  font-size: 1rem;
  line-height: 1.5;
}

.debug-info strong {
  color: var(--ai-title);
  margin-right: 6px;
}

.debug-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--ai-border);
}

/* 修复模型状态信息区域 */
.auto-test-tip, 
.debug-panel {
  margin-top: 20px;
  margin-bottom: 20px;
  z-index: 5;
}

.battle-chat-area {
  flex: 1;
  min-height: 400px; /* 使用最小高度 */
  max-height: none; /* 移除最大高度限制 */
  overflow-y: auto;
  padding: 20px 15px 30px 15px;
  margin-bottom: 80px; /* 为输入框留出空间 */
  scroll-behavior: smooth;
}

.battle-chat-area::-webkit-scrollbar {
  width: 6px;
  background: transparent;
}

.battle-chat-area::-webkit-scrollbar-track {
  background: var(--ai-border);
  border-radius: 3px;
}

.battle-chat-area::-webkit-scrollbar-thumb {
  background: var(--ai-subtitle);
  border-radius: 3px;
}

.battle-chat-area::-webkit-scrollbar-thumb:hover {
  background: var(--ai-title);
}

.battle-row {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px 15px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--header-shadow);
  background: var(--card-bg);
  border: 1px solid var(--ai-border);
  transition: transform 0.3s, box-shadow 0.3s;
  backdrop-filter: blur(10px);
  animation: fadeIn 0.4s ease-out;
}

.battle-row:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.battle-question {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: var(--ai-bubble);
  border-radius: 12px;
  border: 1px solid var(--ai-border);
  transition: all 0.2s;
}

.battle-question:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--bg-color);
}

.battle-replies {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.battle-reply-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: var(--card-bg);
  border-radius: 12px;
  border: 1px solid var(--ai-border);
  transition: all 0.2s;
}

.battle-reply-row:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--bg-color);
}

.battle-reply-row .side-model-label {
  font-size: 1rem;
  font-weight: 600;
  color: var(--ai-title);
  border-bottom: 1px solid var(--ai-border);
  padding-bottom: 8px;
}

.battle-reply-row .side-bubble {
  flex: 1;
  background: var(--card-bg);
  color: var(--ai-text);
  border-radius: 12px;
  padding: 16px;
  font-size: 1.1rem;
  line-height: 1.5;
  border: 1px solid var(--ai-border);
  transition: all 0.2s;
  position: relative;
  word-break: break-word;
}

.battle-reply-row .side-bubble:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: var(--bg-color);
}

.battle-reply-row .model1-col .side-bubble {
  border-top-left-radius: 4px;
}

.battle-reply-row .model2-col .side-bubble {
  border-top-left-radius: 4px;
}

@media (max-width: 1200px) {
  .sparkai-main {
    max-width: 95vw;
    margin: 24px 0;
  }
  .sparkai-header {
    padding: 28px 24px 14px 24px;
    flex-direction: column;
    gap: 16px;
}
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
    justify-content: center;
}
  .sparkai-chat-area {
    padding: 24px 20px 0 20px;
    max-height: 55vh;
  }
  .side-by-side-row {
    flex-direction: column;
    min-width: 0;
    max-width: 95vw;
  }
  .side-col:not(:last-child) {
    border-right: none;
    border-bottom: 2px solid var(--ai-border);
  }
  .side-col {
    padding: 20px 16px;
  }
  .bubble {
    max-width: 85%;
    padding: 16px 20px;
  }
  .sparkai-input-bar {
    padding: 16px 20px;
  }
  .mode-header {
    padding: 16px 20px 0 20px;
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  .mode-header-spacer {
    display: none;
  }
  .new-chat-btn {
    margin-left: 0;
    width: 100%;
  }
  .debug-panel {
    margin: 16px 20px;
  }
  .debug-info {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .sparkai-main {
    max-width: 100vw;
    margin: 0;
    border-radius: 0;
    min-height: 100vh;
    padding: 80px 0 120px 0; /* 在小屏幕上增加更多顶部内边距 */
  }
  
  .sparkai-chat-area,
  .side-by-side-chat-area,
  .battle-chat-area {
    height: calc(100vh - 300px);
    max-height: calc(100vh - 300px);
    margin-bottom: 100px;
  }
  
  .sparkai-input-bar {
    bottom: 30px;
    left: 15px;
    right: 15px;
    padding: 12px 15px;
  }
  .sparkai-header {
    padding: 12px 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .header-left {
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }
  .main-title {
    font-size: 1.4rem;
  }
  .sub-title {
    font-size: 0.95rem;
  }
  .ai-logo {
    width: 48px;
    height: 48px;
  }
  .sparkai-chat-area {
    padding: 20px 12px 0 12px;
    max-height: 50vh;
  }
  .chat-msg {
    gap: 10px;
    margin-bottom: 20px;
  }
  .avatar-wrap {
    width: 40px;
    height: 40px;
  }
  .bubble {
    padding: 14px 16px;
    font-size: 1.05rem;
    max-width: 90%;
  }
  .sparkai-input-bar {
    padding: 12px 12px 12px 12px;
  }
  .chat-input {
    min-height: 20px;
    font-size: 1.05rem;
    padding: 12px 16px;
  }
  .send-btn {
    padding: 12px 20px;
    font-size: 1.05rem;
    min-width: 80px;
  }
  .side-by-side-chat-area {
    padding: 20px 12px 0 12px;
  }
  .side-by-side-row {
    width: 100%;
  }
  .side-col {
    padding: 16px 12px;
  }
  .side-question {
    padding: 14px 16px;
    font-size: 1.05rem;
  }
  .side-bubble {
    padding: 14px 16px;
    font-size: 1.05rem;
  }
  .debug-panel {
    margin: 12px;
    padding: 12px 16px;
  }
}

.ai-loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid var(--ai-border);
  border-radius: 50%;
  border-top-color: var(--ai-title);
  animation: ai-spin 1s linear infinite;
  margin: 0 8px 0 0;
  vertical-align: middle;
}

.test-btn {
  background: var(--ai-bubble-user);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: var(--header-shadow);
  margin-right: 8px;
}

.test-btn:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 5px 15px rgba(78,107,222,0.3);
}

.test-btn:disabled {
  background: var(--ai-border);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.auto-test-tip {
  text-align: center;
  padding: 16px;
  background: var(--card-bg);
  border-radius: 16px;
  margin: 24px 48px;
  box-shadow: var(--header-shadow);
  animation: pulse 2s infinite;
  border: 1px solid var(--ai-border);
}

.loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid var(--ai-border);
  border-radius: 50%;
  border-top-color: var(--ai-title);
  animation: ai-spin 1s linear infinite;
  margin-right: 8px;
}

.lang-btn {
  background: var(--card-bg);
  color: var(--ai-text) !important; /* 确保在任何模式下文字颜色都正确 */
  border: 1px solid var(--ai-border);
  border-radius: 8px;
  font-size: 0.9rem;
  padding: 5px 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.lang-btn:hover {
  background: var(--bg-color);
  transform: translateY(-2px);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes ai-spin {
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(78,107,222,0.4); }
  70% { box-shadow: 0 0 0 10px rgba(78,107,222,0); }
  100% { box-shadow: 0 0 0 0 rgba(78,107,222,0); }
}

:deep([data-theme='dark'] .lang-btn),
:deep([data-theme='dark'] .test-btn),
:deep([data-theme='dark'] .debug-btn),
:deep([data-theme='dark'] .el-button),
:deep([data-theme='dark'] .el-button span),
:deep([data-theme='dark'] .el-select .el-input__inner),
:deep([data-theme='dark'] .el-select-dropdown__item),
:deep([data-theme='dark'] .el-select-dropdown__item span),
:deep([data-theme='dark'] .auto-test-tip),
:deep([data-theme='dark'] .auto-test-tip *),
:deep([data-theme='dark'] .debug-panel),
:deep([data-theme='dark'] .debug-panel *){
  color: #fff !important;
}

[data-theme='dark'] textarea.chat-input,
[data-theme='dark'] .chat-input,
[data-theme='dark'] textarea,
:deep([data-theme='dark'] .chat-input) {
  color: #fff !important;
  caret-color: #fff !important;
  background: var(--ai-bubble) !important;
}
[data-theme='dark'] .chat-input::placeholder,
[data-theme='dark'] textarea.chat-input::placeholder,
[data-theme='dark'] textarea::placeholder {
  color: #bbb !important;
  opacity: 1 !important;
}
[data-theme='dark'] .battle-question {
  background: var(--ai-bubble) !important;
  color: #fff !important;
}
[data-theme='dark'] .battle-question .side-question {
  color: #fff !important;
}

[data-theme='dark'] .el-textarea__inner,
[data-theme='dark'] .el-input__inner {
  color: #fff !important;
  caret-color: #fff !important;
  background: var(--ai-bubble) !important;
}
[data-theme='dark'] .el-textarea__inner::placeholder,
[data-theme='dark'] .el-input__inner::placeholder {
  color: #bbb !important;
  opacity: 1 !important;
}

[data-theme='dark'] .chat-input:focus,
[data-theme='dark'] textarea.chat-input:focus {
  color: #fff !important;
  background: var(--ai-bubble) !important;
  border-color: var(--ai-title) !important;
}
[data-theme='dark'] .el-textarea__inner:focus,
[data-theme='dark'] .el-input__inner:focus {
  color: #fff !important;
  background: var(--ai-bubble) !important;
  border-color: var(--ai-title) !important;
}

[data-theme='dark'] .chat-input,
[data-theme='dark'] .chat-input:focus,
[data-theme='dark'] textarea.chat-input,
[data-theme='dark'] textarea.chat-input:focus,
[data-theme='dark'] .el-textarea__inner,
[data-theme='dark'] .el-textarea__inner:focus {
  background: var(--ai-bubble) !important;
  color: #fff !important;
  caret-color: #fff !important;
  border-color: var(--ai-title) !important;
}
[data-theme='dark'] .chat-input::placeholder,
[data-theme='dark'] textarea.chat-input::placeholder,
[data-theme='dark'] .el-textarea__inner::placeholder {
  color: #bbb !important;
  opacity: 1 !important;
}
[data-theme='light'] .chat-input,
[data-theme='light'] .chat-input:focus,
[data-theme='light'] textarea.chat-input,
[data-theme='light'] textarea.chat-input:focus,
[data-theme='light'] .el-textarea__inner,
[data-theme='light'] .el-textarea__inner:focus {
  background: #fff !important;
  color: #222 !important;
  caret-color: #222 !important;
  border-color: var(--ai-title) !important;
}
[data-theme='light'] .chat-input::placeholder,
[data-theme='light'] textarea.chat-input::placeholder,
[data-theme='light'] .el-textarea__inner::placeholder {
  color: #888 !important;
  opacity: 1 !important;
}
</style> 