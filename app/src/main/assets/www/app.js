/* ================= 基础工具 ================= */
/* 折线图纵轴逻辑坐标：绘图区 8~120，5 等分刻度（4 段）。
   CSS 中 .line-yaxis 的 padding-top/bottom 与这里的 top / (130-bottom) 保持一致 */
const CHART_AXIS = { top: 8, bottom: 120, span: 112, steps: 4 };
const pad = n => String(n).padStart(2, '0');
const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const parseDate = s => {
  const [y, m, d] = s.split('-').map(Number);
  return new Date(y, m - 1, d);
};
const diffDays = (a, b) => Math.round((parseDate(b) - parseDate(a)) / 86400000);
const addDays = (s, n) => {
  const d = parseDate(s);
  d.setDate(d.getDate() + n);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const ymOf = s => s.slice(0, 7);
const ymAdd = (ym, n) => {
  const [y, m] = ym.split('-').map(Number);
  const d = new Date(y, m - 1 + n, 1);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;
};
const ymDiff = (a, b) => {
  const [y1, m1] = a.split('-').map(Number);
  const [y2, m2] = b.split('-').map(Number);
  return (y2 - y1) * 12 + (m2 - m1);
};
const ymStart = ym => `${ym}-01`;
const ymEnd = ym => {
  const [y, m] = ym.split('-').map(Number);
  return `${ym}-${pad(new Date(y, m, 0).getDate())}`;
};
const ymMonth = ym => Number(ym.split('-')[1]);
const ymYear = ym => Number(ym.split('-')[0]);

const colorOf = v => {
  if (typeof v === 'string' && v.startsWith('#')) return v;
  const n = Number(v) >>> 0;
  const r = (n >> 16) & 255, g = (n >> 8) & 255, b = n & 255;
  return '#' + [r, g, b].map(x => x.toString(16).padStart(2, '0')).join('');
};
function hexA(hex, a) {
  const n = parseInt(hex.slice(1), 16);
  return `rgba(${(n >> 16) & 255}, ${(n >> 8) & 255}, ${n & 255}, ${a})`;
}

const ACCENTS = [
  { c: '#2563EB', c2: '#60A5FA' },
  { c: '#059669', c2: '#34D399' },
  { c: '#7C3AED', c2: '#A78BFA' },
  { c: '#EA580C', c2: '#FB923C' },
  { c: '#DB2777', c2: '#F472B6' }
];

/* ================= 图标库 ================= */
const ICON_GROUPS = [
  { k: 'digital', n: '数码' },
  { k: 'home', n: '家居' },
  { k: 'wear', n: '服饰' },
  { k: 'food', n: '饮食' },
  { k: 'move', n: '出行' },
  { k: 'sport', n: '运动' },
  { k: 'fun', n: '娱乐' },
  { k: 'more', n: '其他' }
];

const ICONS = [
  /* ---------- 数码 ---------- */
  { k: 'phone', n: '手机', g: 'digital', v:
    '<rect class="d" x="6.5" y="2.5" width="11" height="19" rx="3.2"/>' +
    '<rect x="6.5" y="2.5" width="11" height="19" rx="3.2"/>' +
    '<path d="M10.6 4.9h2.8"/>' +
    '<path d="M10.5 18.6h3"/>' },

  { k: 'laptop', n: '电脑', g: 'digital', v:
    '<rect class="d" x="4.5" y="4.5" width="15" height="10.8" rx="1.8"/>' +
    '<rect x="4.5" y="4.5" width="15" height="10.8" rx="1.8"/>' +
    '<path d="M2.5 18.6h19"/>' +
    '<path d="M3.9 15.3h16.2"/>' },

  { k: 'headphone', n: '耳机', g: 'digital', v:
    '<rect class="d" x="2.8" y="13" width="4.9" height="7.6" rx="2.4"/>' +
    '<rect class="d" x="16.3" y="13" width="4.9" height="7.6" rx="2.4"/>' +
    '<path d="M4.6 15.5v-3a7.4 7.4 0 0 1 14.8 0v3"/>' +
    '<rect x="2.8" y="13" width="4.9" height="7.6" rx="2.4"/>' +
    '<rect x="16.3" y="13" width="4.9" height="7.6" rx="2.4"/>' },

  { k: 'camera', n: '相机', g: 'digital', v:
    '<rect class="d" x="2.5" y="7" width="19" height="12.5" rx="3"/>' +
    '<path d="M8.3 7 9.7 4.4h4.6L15.7 7"/>' +
    '<rect x="2.5" y="7" width="19" height="12.5" rx="3"/>' +
    '<circle cx="12" cy="13" r="3.4"/>' +
    '<circle class="a" cx="18.6" cy="10.1" r=".9"/>' },

  { k: 'tv', n: '显示设备', g: 'digital', v:
    '<rect class="d" x="2.5" y="4.5" width="19" height="13" rx="2.6"/>' +
    '<rect x="2.5" y="4.5" width="19" height="13" rx="2.6"/>' +
    '<path d="M12 17.5V21"/>' +
    '<path d="M8.3 21h7.4"/>' },

  { k: 'keyboard', n: '键盘', g: 'digital', v:
    '<rect class="d" x="2.5" y="6.5" width="19" height="11" rx="2.5"/>' +
    '<rect x="2.5" y="6.5" width="19" height="11" rx="2.5"/>' +
    '<path d="M6.2 10.3h.01M9.4 10.3h.01M12.6 10.3h.01M15.8 10.3h.01M18 10.3h.01M6.2 13.6h.01M18 13.6h.01"/>' +
    '<path d="M8.4 13.6h7.2"/>' },

  { k: 'watch', n: '手表', g: 'digital', v:
    '<circle class="d" cx="12" cy="12" r="5.6"/>' +
    '<circle cx="12" cy="12" r="5.6"/>' +
    '<path d="M12 9.5v2.7l1.9 1.1"/>' +
    '<path d="M9.4 6.9 9.8 2.7h4.4l.4 4.2M9.4 17.1l.4 4.2h4.4l.4-4.2"/>' },

  { k: 'speaker', n: '音箱', g: 'digital', v:
    '<rect class="d" x="6" y="2.5" width="12" height="19" rx="3.2"/>' +
    '<rect x="6" y="2.5" width="12" height="19" rx="3.2"/>' +
    '<circle cx="12" cy="14.3" r="3.3"/>' +
    '<circle class="a" cx="12" cy="14.3" r="1.1"/>' +
    '<circle cx="12" cy="7" r="1.4"/>' },

  /* ---------- 家居 ---------- */
  { k: 'sofa', n: '家具', g: 'home', v:
    '<rect class="d" x="4.5" y="9" width="15" height="4.8" rx="1.6"/>' +
    '<path d="M4.5 12.6V9.2A2.2 2.2 0 0 1 6.7 7h10.6a2.2 2.2 0 0 1 2.2 2.2v3.4"/>' +
    '<path d="M2.5 16v-1.2a2.1 2.1 0 0 1 2.1-2.1h14.8a2.1 2.1 0 0 1 2.1 2.1V16a2.3 2.3 0 0 1-2.3 2.3H4.8A2.3 2.3 0 0 1 2.5 16z"/>' +
    '<path d="M5.2 18.5v1.6M18.8 18.5v1.6"/>' },

  { k: 'lamp', n: '灯具', g: 'home', v:
    '<path class="d" d="M8.2 3.5h7.6l1.7 5.4H6.5z"/>' +
    '<path d="M8.2 3.5h7.6l1.7 5.4H6.5z"/>' +
    '<path d="M12 8.9v10.3"/>' +
    '<path d="M7.4 19.2h9.2"/>' },

  { k: 'cup', n: '杯具', g: 'home', v:
    '<path class="d" d="M5.5 8.8h10.4v7.9a3.6 3.6 0 0 1-3.6 3.6H9.1a3.6 3.6 0 0 1-3.6-3.6z"/>' +
    '<path d="M5.5 8.8h10.4v7.9a3.6 3.6 0 0 1-3.6 3.6H9.1a3.6 3.6 0 0 1-3.6-3.6z"/>' +
    '<path d="M15.9 10.7h1.3a2.4 2.4 0 0 1 0 4.8h-1.3"/>' +
    '<path d="M9.3 2.8c-.7.9.7 1.5 0 2.4M12.7 2.8c-.7.9.7 1.5 0 2.4"/>' },

  { k: 'pot', n: '厨具', g: 'home', v:
    '<path class="d" d="M4.7 10.2h14.6v5.1a4.2 4.2 0 0 1-4.2 4.2H8.9a4.2 4.2 0 0 1-4.2-4.2z"/>' +
    '<path d="M4.7 10.2h14.6v5.1a4.2 4.2 0 0 1-4.2 4.2H8.9a4.2 4.2 0 0 1-4.2-4.2z"/>' +
    '<path d="M5.2 10.2c.4-2.1 3.2-3.4 6.8-3.4s6.4 1.3 6.8 3.4"/>' +
    '<path d="M12 4.4v2.3"/>' +
    '<path d="M9.9 1.9c-.5.7.5 1.1 0 1.8M14.1 1.9c-.5.7.5 1.1 0 1.8"/>' },

  { k: 'plant', n: '绿植', g: 'home', v:
    '<path class="d" d="M8 15.3h8l-.8 4.1a1.6 1.6 0 0 1-1.6 1.3h-3.2a1.6 1.6 0 0 1-1.6-1.3z"/>' +
    '<path d="M8 15.3h8l-.8 4.1a1.6 1.6 0 0 1-1.6 1.3h-3.2a1.6 1.6 0 0 1-1.6-1.3z"/>' +
    '<path d="M7.4 15.3h9.2"/>' +
    '<path d="M12 15.3V9.2"/>' +
    '<path d="M12 11.3C9.2 11.3 7.5 9.7 7.1 6.5c3 .3 4.6 1.9 4.9 4.8z"/>' +
    '<path d="M12 9.4c0-2.7 1.6-4.3 4.5-4.6-.3 2.9-1.9 4.6-4.5 4.6z"/>' },

  { k: 'box', n: '包裹', g: 'home', v:
    '<path class="d" d="M4.3 8.7 12 12.2l7.7-3.5v6.9L12 19.4l-7.7-3.8z"/>' +
    '<path d="M4.3 8.5 12 4.7l7.7 3.8v7L12 19.4l-7.7-3.8z"/>' +
    '<path d="M4.3 8.5 12 12.2l7.7-3.7M12 12.2v7.2"/>' +
    '<path d="M8.2 6.6v4.4"/>' },

  /* ---------- 服饰 ---------- */
  { k: 'shirt', n: '衣物', g: 'wear', v:
    '<path class="d" d="M8.8 11.4h6.4v9.1H8.8z"/>' +
    '<path d="M8.7 4 5.4 5.6 3.4 9.7l3.1 1.5V20.6h11V11.2l3.1-1.5-2-4.1L15.3 4a3.3 3.3 0 0 1-6.6 0z"/>' },

  { k: 'bag', n: '包袋', g: 'wear', v:
    '<rect class="d" x="4.8" y="9.4" width="14.4" height="11.1" rx="2.2"/>' +
    '<rect x="4.8" y="9.4" width="14.4" height="11.1" rx="2.2"/>' +
    '<path d="M8.6 12.4V7.2a3.4 3.4 0 0 1 6.8 0v5.2"/>' },

  { k: 'glasses', n: '眼镜', g: 'wear', v:
    '<circle class="d" cx="7.1" cy="13.6" r="3.5"/>' +
    '<circle class="d" cx="16.9" cy="13.6" r="3.5"/>' +
    '<circle cx="7.1" cy="13.6" r="3.5"/>' +
    '<circle cx="16.9" cy="13.6" r="3.5"/>' +
    '<path d="M10.6 13.2c.8-1 2-1 2.8 0"/>' +
    '<path d="M3.6 13.3 2.3 10.6M20.4 13.3l1.3-2.7"/>' },

  { k: 'umbrella', n: '伞具', g: 'wear', v:
    '<path class="d" d="M12 3.4c5.2 0 8.7 3.5 8.8 7.6H3.2C3.3 6.9 6.8 3.4 12 3.4z"/>' +
    '<path d="M12 3.4c5.2 0 8.7 3.5 8.8 7.6H3.2C3.3 6.9 6.8 3.4 12 3.4z"/>' +
    '<path d="M12 2.2v1.2"/>' +
    '<path d="M12 11v8.8a1.9 1.9 0 0 1-3.8 0"/>' },

  /* ---------- 饮食 ---------- */
  { k: 'apple', n: '水果', g: 'food', v:
    '<path class="d" d="M12 8.6c2.9-1.9 6.6-.5 6.6 3.4 0 3.9-2.9 8.2-5.1 8.2-.9 0-1.2-.5-1.5-.5s-.6.5-1.5.5c-2.2 0-5.1-4.3-5.1-8.2 0-3.9 3.7-5.3 6.6-3.4z"/>' +
    '<path d="M12 8.6c2.9-1.9 6.6-.5 6.6 3.4 0 3.9-2.9 8.2-5.1 8.2-.9 0-1.2-.5-1.5-.5s-.6.5-1.5.5c-2.2 0-5.1-4.3-5.1-8.2 0-3.9 3.7-5.3 6.6-3.4z"/>' +
    '<path d="M12 8.4c-.2-2 .7-3.3 2.4-4"/>' +
    '<path d="M14.6 5.5c.3-1.3 1.2-2.1 2.7-2.3-.2 1.4-1.2 2.2-2.7 2.3z"/>' },

  { k: 'drink', n: '饮品', g: 'food', v:
    '<path class="d" d="M7 12.8h10l-1 6.4a2 2 0 0 1-2 1.7h-4a2 2 0 0 1-2-1.7z"/>' +
    '<path d="M6.4 8.9h11.2l-1.2 10.3a2 2 0 0 1-2 1.7H9.6a2 2 0 0 1-2-1.7z"/>' +
    '<path d="M13.9 8.9 15.9 2.4h2.3"/>' +
    '<path d="M7 12.8h10"/>' +
    '<circle class="a" cx="10.3" cy="15.6" r=".9"/>' +
    '<circle class="a" cx="13.6" cy="17.4" r=".9"/>' },

  { k: 'utensils', n: '餐饮', g: 'food', v:
    '<path class="d" d="M17 11.6c-1.6-2.6-1.5-5.9.4-8.8h1.3v8.8z"/>' +
    '<path d="M6.9 2.8v5.9"/>' +
    '<path d="M4.7 2.8v3.4a2.2 2.2 0 0 0 4.4 0V2.8"/>' +
    '<path d="M6.9 8.7v12.5"/>' +
    '<path d="M17 11.6v9.6"/>' +
    '<path d="M17 11.6c-1.6-2.6-1.5-5.9.4-8.8h1.3v8.8z"/>' },

  /* ---------- 出行 ---------- */
  { k: 'car', n: '汽车', g: 'move', v:
    '<path class="d" d="M5.4 12.4 6.6 9a1.6 1.6 0 0 1 1.5-1.1h7.8a1.6 1.6 0 0 1 1.5 1.1l1.2 3.4z"/>' +
    '<path d="M4.4 12.4 5.9 8.3A2 2 0 0 1 7.8 7h8.4a2 2 0 0 1 1.9 1.3l1.5 4.1"/>' +
    '<path d="M4.4 12.4h15.2"/>' +
    '<path d="M4.4 12.4v4.5a1.3 1.3 0 0 0 1.3 1.3h12.6a1.3 1.3 0 0 0 1.3-1.3v-4.5"/>' +
    '<circle cx="7.4" cy="18.2" r="1.7"/>' +
    '<circle cx="16.6" cy="18.2" r="1.7"/>' },

  { k: 'bike', n: '单车', g: 'move', v:
    '<circle cx="5.6" cy="16.5" r="3.3"/>' +
    '<circle cx="18.4" cy="16.5" r="3.3"/>' +
    '<path d="M12 16.5v-2.9l-3-3 4-3 2 3h2.4"/>' +
    '<circle class="a" cx="15" cy="5.6" r="1.1"/>' },

  { k: 'motor', n: '摩托车', g: 'move', v:
    '<path class="d" d="M8.4 16.9c.3-3.2 2.6-4.8 5.9-4.8h3.6l.7 4.8z"/>' +
    '<circle cx="5.4" cy="16.9" r="2.7"/>' +
    '<circle cx="18.6" cy="16.9" r="2.7"/>' +
    '<path d="M5.4 16.9h4.8l1.8-4.6h4.3"/>' +
    '<path d="M16.5 5.5 18.6 16.9"/>' +
    '<path d="M15 5.5h3"/>' },

  { k: 'plane', n: '飞机', g: 'move', v:
    '<path class="d" d="M12 3c.9 0 1.5.8 1.5 1.7v5l6.8 4v2l-6.8-2.1v4l2 1.6v1.5L12 19.6l-3.5 1.1v-1.5l2-1.6v-4L3.7 15.7v-2l6.8-4v-5c0-.9.6-1.7 1.5-1.7z"/>' +
    '<path d="M12 3c.9 0 1.5.8 1.5 1.7v5l6.8 4v2l-6.8-2.1v4l2 1.6v1.5L12 19.6l-3.5 1.1v-1.5l2-1.6v-4L3.7 15.7v-2l6.8-4v-5c0-.9.6-1.7 1.5-1.7z"/>' },

  { k: 'train', n: '地铁火车', g: 'move', v:
    '<rect class="d" x="5.2" y="3" width="13.6" height="14" rx="3"/>' +
    '<rect x="5.2" y="3" width="13.6" height="14" rx="3"/>' +
    '<path d="M8.4 6.4h7.2v4.2H8.4z"/>' +
    '<path d="M8.7 13.6h.01M15.3 13.6h.01"/>' +
    '<path d="M9.3 19.4 7.2 21.6M14.7 19.4l2.1 2.2M5.4 21.6h13.2"/>' },

  /* ---------- 运动 ---------- */
  { k: 'dumbbell', n: '健身', g: 'sport', v:
    '<rect class="d" x="3.4" y="7.4" width="3.5" height="9.2" rx="1.4"/>' +
    '<rect class="d" x="17.1" y="7.4" width="3.5" height="9.2" rx="1.4"/>' +
    '<rect x="6.9" y="5.6" width="3.4" height="12.8" rx="1.4"/>' +
    '<rect x="13.7" y="5.6" width="3.4" height="12.8" rx="1.4"/>' +
    '<path d="M10.3 12h3.4"/>' },

  { k: 'mountain', n: '户外', g: 'sport', v:
    '<path class="d" d="M2.6 19 9 8.2l3.2 5.3L15 10l6.4 9z"/>' +
    '<path d="M2.6 19 9 8.2l3.2 5.3M10.9 19l5.4-8L21.4 19z"/>' +
    '<path d="M2.6 19h18.8"/>' +
    '<circle class="a" cx="17.8" cy="5.4" r="1.2"/>' },

  { k: 'ball', n: '球类', g: 'sport', v:
    '<circle class="d" cx="12" cy="12" r="7.8"/>' +
    '<circle cx="12" cy="12" r="7.8"/>' +
    '<path d="M12 4.2v15.6"/>' +
    '<path d="M4.9 8.1c2 1.7 4.3 2.5 7.1 2.5s5.1-.8 7.1-2.5M4.9 15.9c2-1.7 4.3-2.5 7.1-2.5s5.1.8 7.1 2.5"/>' },

  { k: 'tent', n: '露营', g: 'sport', v:
    '<path class="d" d="M12 5.6 20.6 19H3.4z"/>' +
    '<path d="M3.4 19 12 5.6 20.6 19z"/>' +
    '<path d="M12 19v-5.2"/>' +
    '<path d="M9.4 19 12 13.8l2.6 5.2"/>' +
    '<path d="M2 19h20"/>' },

  /* ---------- 娱乐 ---------- */
  { k: 'book', n: '书籍', g: 'fun', v:
    '<path class="d" d="M12 6.6C10.1 5.1 7.7 4.6 4.7 4.6v12.8c3 0 5.4.5 7.3 1.9 1.9-1.4 4.3-1.9 7.3-1.9V4.6c-3 0-5.4.5-7.3 2z"/>' +
    '<path d="M12 6.6C10.1 5.1 7.7 4.6 4.7 4.6v12.8c3 0 5.4.5 7.3 1.9 1.9-1.4 4.3-1.9 7.3-1.9V4.6c-3 0-5.4.5-7.3 2z"/>' +
    '<path d="M12 6.6v12.7"/>' +
    '<path d="M7.2 8.4h2.2M7.2 11h2.2M14.6 8.4h2.2M14.6 11h2.2"/>' },

  { k: 'music', n: '音乐', g: 'fun', v:
    '<circle class="d" cx="7" cy="17.2" r="2.5"/>' +
    '<circle class="d" cx="16.8" cy="14.9" r="2.5"/>' +
    '<circle cx="7" cy="17.2" r="2.5"/>' +
    '<circle cx="16.8" cy="14.9" r="2.5"/>' +
    '<path d="M9.5 17.2V6.6l9.8-2.2v10.5"/>' },

  { k: 'film', n: '影音', g: 'fun', v:
    '<rect class="d" x="3" y="4.6" width="18" height="14.8" rx="2.4"/>' +
    '<rect x="3" y="4.6" width="18" height="14.8" rx="2.4"/>' +
    '<path d="M7.8 4.6v14.8M16.2 4.6v14.8"/>' +
    '<path d="M7.8 9.2h8.4M7.8 14.8h8.4"/>' +
    '<circle class="a" cx="5.4" cy="6.9" r=".8"/>' +
    '<circle class="a" cx="5.4" cy="12" r=".8"/>' +
    '<circle class="a" cx="5.4" cy="17.1" r=".8"/>' +
    '<circle class="a" cx="18.6" cy="6.9" r=".8"/>' +
    '<circle class="a" cx="18.6" cy="12" r=".8"/>' +
    '<circle class="a" cx="18.6" cy="17.1" r=".8"/>' },

  { k: 'gamepad', n: '游戏', g: 'fun', v:
    '<path class="d" d="M6.8 6.8h10.4c2.8 0 4.9 2.3 4.9 5.2 0 2.8-1.4 5.2-3.2 5.2-1.3 0-2-.7-2.8-1.7-.6-.7-1.1-1-1.9-1h-4.4c-.8 0-1.3.3-1.9 1-.8 1-1.5 1.7-2.8 1.7-1.8 0-3.2-2.4-3.2-5.2 0-2.9 2.1-5.2 4.9-5.2z"/>' +
    '<path d="M6.8 6.8h10.4c2.8 0 4.9 2.3 4.9 5.2 0 2.8-1.4 5.2-3.2 5.2-1.3 0-2-.7-2.8-1.7-.6-.7-1.1-1-1.9-1h-4.4c-.8 0-1.3.3-1.9 1-.8 1-1.5 1.7-2.8 1.7-1.8 0-3.2-2.4-3.2-5.2 0-2.9 2.1-5.2 4.9-5.2z"/>' +
    '<path d="M7.7 10.1v3.8M5.8 12h3.8"/>' +
    '<circle class="a" cx="16.5" cy="11" r="1"/>' +
    '<circle class="a" cx="18.4" cy="13.2" r="1"/>' },

  /* ---------- 其他 ---------- */
  { k: 'heart', n: '爱心', g: 'more', v:
    '<path class="d" d="M12 20.4C7.2 16.5 3.2 13.3 3.2 9.4c0-2.9 2.2-4.8 4.5-4.8 1.8 0 3.3 1 4.3 2.7 1-1.7 2.5-2.7 4.3-2.7 2.3 0 4.5 1.9 4.5 4.8 0 3.9-4 7.1-8.8 11z"/>' +
    '<path d="M12 20.4C7.2 16.5 3.2 13.3 3.2 9.4c0-2.9 2.2-4.8 4.5-4.8 1.8 0 3.3 1 4.3 2.7 1-1.7 2.5-2.7 4.3-2.7 2.3 0 4.5 1.9 4.5 4.8 0 3.9-4 7.1-8.8 11z"/>' +
    '<path d="M8.3 8.1c-1.1.3-1.9 1.1-2.2 2.2"/>' },

  { k: 'gift', n: '礼物', g: 'more', v:
    '<rect class="d" x="4.6" y="11.2" width="14.8" height="9.3" rx="1.4"/>' +
    '<rect x="4.6" y="11.2" width="14.8" height="9.3" rx="1.4"/>' +
    '<rect x="3.6" y="7.6" width="16.8" height="3.6" rx="1.1"/>' +
    '<path d="M12 7.6v12.9"/>' +
    '<path d="M12 7.4C12 5 10.6 3.4 9.1 3.4c-1.4 0-2.2 1.3-1.4 2.6.7 1.1 2.2 1.4 4.3 1.4z"/>' +
    '<path d="M12 7.4c0-2.4 1.4-4 2.9-4 1.4 0 2.2 1.3 1.4 2.6-.7 1.1-2.2 1.4-4.3 1.4z"/>' },

  { k: 'star', n: '收藏', g: 'more', v:
    '<path class="d" d="m12 3.4 2.5 5.2 5.7.8-4.1 4 1 5.6-5.1-2.7-5.1 2.7 1-5.6-4.1-4 5.7-.8z"/>' +
    '<path d="m12 3.4 2.5 5.2 5.7.8-4.1 4 1 5.6-5.1-2.7-5.1 2.7 1-5.6-4.1-4 5.7-.8z"/>' },

  { k: 'key', n: '钥匙', g: 'more', v:
    '<circle class="d" cx="8" cy="8.2" r="3.4"/>' +
    '<circle cx="8" cy="8.2" r="3.4"/>' +
    '<circle class="a" cx="8" cy="8.2" r="1"/>' +
    '<path d="m10.5 10.7 8.6 8.6"/>' +
    '<path d="m16.2 16.4 1.6-1.6M18.4 18.6l1.6-1.6"/>' },

  { k: 'wallet', n: '钱包', g: 'more', v:
    '<rect class="d" x="3" y="6" width="18" height="13" rx="2.6"/>' +
    '<path d="M3 9.3h18"/>' +
    '<rect x="3" y="6" width="18" height="13" rx="2.6"/>' +
    '<path d="M14.6 11.9h6.4v3.4h-6.4a1.7 1.7 0 0 1 0-3.4z"/>' +
    '<circle class="a" cx="16.7" cy="13.6" r=".8"/>' },

  { k: 'pill', n: '药品', g: 'more', v:
    '<path class="d" d="M8.5 8.5l7 7 3.5-3.5a4.95 4.95 0 1 0-7-7z"/>' +
    '<path d="m10.5 20.5 10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z"/>' +
    '<path d="m8.5 8.5 7 7"/>' },

  { k: 'sparkle', n: '兴趣', g: 'more', v:
    '<path class="d" d="M11.2 4.2c.6 3.7 2.3 5.4 6 6-3.7.6-5.4 2.3-6 6-.6-3.7-2.3-5.4-6-6 3.7-.6 5.4-2.3 6-6z"/>' +
    '<path d="M11.2 4.2c.6 3.7 2.3 5.4 6 6-3.7.6-5.4 2.3-6 6-.6-3.7-2.3-5.4-6-6 3.7-.6 5.4-2.3 6-6z"/>' +
    '<path d="M18.6 14.6c.3 1.9 1.2 2.8 3.1 3.1-1.9.3-2.8 1.2-3.1 3.1-.3-1.9-1.2-2.8-3.1-3.1 1.9-.3 2.8-1.2 3.1-3.1z"/>' },

  /* ---------- 数码 · 补充 ---------- */
  { k: 'tablet', n: '平板', g: 'digital', v:
    '<rect class="d" x="5" y="2.5" width="14" height="19" rx="3.4"/>' +
    '<rect x="5" y="2.5" width="14" height="19" rx="3.4"/>' +
    '<circle class="a" cx="12" cy="18.4" r="1"/>' +
    '<path d="M10.6 4.8h2.8"/>' },

  { k: 'mouse', n: '鼠标', g: 'digital', v:
    '<path class="d" d="M12 2.8c3.7 0 6.2 2.7 6.2 6.8v4.8c0 4.1-2.5 6.8-6.2 6.8s-6.2-2.7-6.2-6.8V9.6c0-4.1 2.5-6.8 6.2-6.8z"/>' +
    '<path d="M12 2.8c3.7 0 6.2 2.7 6.2 6.8v4.8c0 4.1-2.5 6.8-6.2 6.8s-6.2-2.7-6.2-6.8V9.6c0-4.1 2.5-6.8 6.2-6.8z"/>' +
    '<path d="M12 6.2v3.2"/>' +
    '<path d="M5.8 9.6h12.4"/>' },

  { k: 'printer', n: '打印机', g: 'digital', v:
    '<rect class="d" x="4.2" y="8.8" width="15.6" height="7.4" rx="2"/>' +
    '<rect x="4.2" y="8.8" width="15.6" height="7.4" rx="2"/>' +
    '<path d="M7 8.8V3.4h10v5.4"/>' +
    '<path d="M8 16.2v4.4h8v-4.4"/>' +
    '<circle class="a" cx="17.4" cy="11.6" r=".9"/>' +
    '<path d="M9.8 11.6H6.8"/>' },

  { k: 'powerbank', n: '充电宝', g: 'digital', v:
    '<rect class="d" x="6.6" y="2.6" width="10.8" height="18.8" rx="3.2"/>' +
    '<rect x="6.6" y="2.6" width="10.8" height="18.8" rx="3.2"/>' +
    '<path d="M9.4 5.6h1.7M12.9 5.6h1.7"/>' +
    '<circle class="a" cx="12" cy="13.8" r="1.8"/>' +
    '<path d="M10.4 18.4h3.2"/>' },

  { k: 'router', n: '路由器', g: 'digital', v:
    '<rect class="d" x="2.8" y="12.4" width="18.4" height="7" rx="2"/>' +
    '<rect x="2.8" y="12.4" width="18.4" height="7" rx="2"/>' +
    '<path d="M8 12.4V4.4M16 12.4V4.4"/>' +
    '<circle class="a" cx="6.6" cy="15.9" r=".9"/>' +
    '<path d="M10 15.9h7.6"/>' },

  { k: 'drone', n: '无人机', g: 'digital', v:
    '<rect class="d" x="9" y="9" width="6" height="6" rx="1.8"/>' +
    '<circle cx="5.4" cy="5.4" r="2.4"/>' +
    '<circle cx="18.6" cy="5.4" r="2.4"/>' +
    '<circle cx="5.4" cy="18.6" r="2.4"/>' +
    '<circle cx="18.6" cy="18.6" r="2.4"/>' +
    '<path d="M7.2 7.2l2.4 2.4M16.8 7.2l-2.4 2.4M7.2 16.8l2.4-2.4M16.8 16.8l-2.4-2.4"/>' +
    '<rect x="9" y="9" width="6" height="6" rx="1.8"/>' },

  /* ---------- 家居 · 补充 ---------- */
  { k: 'bed', n: '床品', g: 'home', v:
    '<rect class="d" x="3" y="12.6" width="18" height="5.8" rx="1.6"/>' +
    '<rect x="3" y="12.6" width="18" height="5.8" rx="1.6"/>' +
    '<path d="M4.6 12.6V6M4.6 9.6h14.8M19.4 12.6V9.6"/>' +
    '<rect class="a" x="6.6" y="10.4" width="4.8" height="2.2" rx="1.1"/>' },

  { k: 'chair', n: '座椅', g: 'home', v:
    '<rect class="d" x="7.2" y="3.4" width="9.6" height="8.6" rx="1.6"/>' +
    '<rect x="7.2" y="3.4" width="9.6" height="8.6" rx="1.6"/>' +
    '<path d="M5 12v2.4a1.6 1.6 0 0 0 1.6 1.6h10.8a1.6 1.6 0 0 0 1.6-1.6V12"/>' +
    '<path d="M6.6 16v4.6M17.4 16v4.6"/>' },

  { k: 'fridge', n: '冰箱', g: 'home', v:
    '<rect class="d" x="5.6" y="2.6" width="12.8" height="18.8" rx="2.4"/>' +
    '<rect x="5.6" y="2.6" width="12.8" height="18.8" rx="2.4"/>' +
    '<path d="M5.6 9.6h12.8"/>' +
    '<path d="M8.4 5.4v2.4M8.4 12.4v3.2"/>' },

  { k: 'washer', n: '洗衣机', g: 'home', v:
    '<rect class="d" x="3.8" y="3.2" width="16.4" height="17.6" rx="2.6"/>' +
    '<rect x="3.8" y="3.2" width="16.4" height="17.6" rx="2.6"/>' +
    '<circle cx="12" cy="13.8" r="4"/>' +
    '<circle class="a" cx="12" cy="13.8" r="1.7"/>' +
    '<circle class="a" cx="7.3" cy="6.4" r=".9"/>' +
    '<path d="M13.6 6.4h3.4"/>' },

  { k: 'aircon', n: '空调', g: 'home', v:
    '<rect class="d" x="2.8" y="4.6" width="18.4" height="7" rx="2.4"/>' +
    '<rect x="2.8" y="4.6" width="18.4" height="7" rx="2.4"/>' +
    '<circle class="a" cx="6.4" cy="8.1" r=".8"/>' +
    '<path d="M14.8 8.1h3.6"/>' +
    '<path d="M7 15.4c-.7.9.7 1.7 0 2.6M12 15.4c-.7.9.7 1.7 0 2.6M17 15.4c-.7.9.7 1.7 0 2.6"/>' },

  { k: 'clock', n: '时钟', g: 'home', v:
    '<circle class="d" cx="12" cy="12" r="8"/>' +
    '<circle cx="12" cy="12" r="8"/>' +
    '<path d="M12 7.6V12l3.1 1.9"/>' +
    '<circle class="a" cx="12" cy="12" r="1"/>' },

  { k: 'mirror', n: '镜子', g: 'home', v:
    '<rect class="d" x="7" y="2.6" width="10" height="14.8" rx="5"/>' +
    '<rect x="7" y="2.6" width="10" height="14.8" rx="5"/>' +
    '<path d="M9.6 6.2c-.9 1.1-1.3 2.3-1.2 3.8"/>' +
    '<path d="M12 17.4v3.2"/>' +
    '<path d="M8.6 20.6h6.8"/>' },

  /* ---------- 饮食 · 补充 ---------- */
  { k: 'coffee', n: '咖啡', g: 'food', v:
    '<path class="d" d="M5.6 9.8h9.6v5a4.2 4.2 0 0 1-4.2 4.2H9.8a4.2 4.2 0 0 1-4.2-4.2z"/>' +
    '<path d="M5.6 9.8h9.6v5a4.2 4.2 0 0 1-4.2 4.2H9.8a4.2 4.2 0 0 1-4.2-4.2z"/>' +
    '<path d="M15.2 11h1.4a2.7 2.7 0 0 1 0 5.4h-1.6"/>' +
    '<path d="M9 3.2c-.6.9.6 1.5 0 2.4M12.4 3.2c-.6.9.6 1.5 0 2.4"/>' },

  { k: 'wine', n: '酒具', g: 'food', v:
    '<path class="d" d="M6.4 3.8h11.2v3.2c0 3.2-2.5 5.6-5.6 5.6s-5.6-2.4-5.6-5.6z"/>' +
    '<path d="M6.4 3.8h11.2v3.2c0 3.2-2.5 5.6-5.6 5.6s-5.6-2.4-5.6-5.6z"/>' +
    '<path d="M12 12.6v6.2"/>' +
    '<path d="M8.6 20.8h6.8"/>' },

  { k: 'kettle', n: '水壶', g: 'food', v:
    '<path class="d" d="M7.6 9.8h8.8l.9 7.8a2.6 2.6 0 0 1-2.6 2.9H9.3a2.6 2.6 0 0 1-2.6-2.9z"/>' +
    '<path d="M7.6 9.8h8.8l.9 7.8a2.6 2.6 0 0 1-2.6 2.9H9.3a2.6 2.6 0 0 1-2.6-2.9z"/>' +
    '<path d="M7.4 11.6H4.8l1.4-2.4"/>' +
    '<path d="M8.8 9.8a3.2 3.2 0 0 1 6.4 0"/>' +
    '<path d="M10.9 5.6h2.2"/>' },

  { k: 'noodle', n: '面食', g: 'food', v:
    '<path class="d" d="M4.2 12.4h15.6v1.4a5.8 5.8 0 0 1-5.8 5.8H10a5.8 5.8 0 0 1-5.8-5.8z"/>' +
    '<path d="M4.2 12.4h15.6v1.4a5.8 5.8 0 0 1-5.8 5.8H10a5.8 5.8 0 0 1-5.8-5.8z"/>' +
    '<path d="M8 12.4V7.6M12 12.4V6.6M16 12.4V7.6"/>' +
    '<path d="M5.8 6.6 18.6 2.8M7.6 8.6 19.8 5.4"/>' },

  { k: 'cake', n: '甜点', g: 'food', v:
    '<rect class="d" x="4.4" y="13.2" width="15.2" height="7" rx="1.8"/>' +
    '<rect x="4.4" y="13.2" width="15.2" height="7" rx="1.8"/>' +
    '<rect x="7" y="9.8" width="10" height="3.4" rx="1.2"/>' +
    '<path d="M12 9.8V7.2"/>' +
    '<circle class="a" cx="12" cy="5.6" r="1.1"/>' },

  /* ---------- 服饰 · 补充 ---------- */
  { k: 'shoe', n: '鞋靴', g: 'wear', v:
    '<path class="d" d="M3.2 16.2v-2.4c0-.6.4-1 1-1h3c2.9 0 4.6-1.5 5.6-3.4.3-.6 1.2-.7 1.6-.1.9 1.4 2.3 2.5 4.4 3.1 1.5.4 2.4 1.3 2.4 2.7v1.1z"/>' +
    '<path d="M3.2 16.2v-2.4c0-.6.4-1 1-1h3c2.9 0 4.6-1.5 5.6-3.4.3-.6 1.2-.7 1.6-.1.9 1.4 2.3 2.5 4.4 3.1 1.5.4 2.4 1.3 2.4 2.7v1.1z"/>' +
    '<path d="M3.2 16.2h18.4"/>' +
    '<path d="M3.2 19.8v-1.6a1 1 0 0 1 1-1h16.4a1 1 0 0 1 1 1v1.6z"/>' +
    '<path d="M10.4 11.4l1.4 1.4M12.8 9.8l1.4 1.4"/>' },

  { k: 'hat', n: '帽子', g: 'wear', v:
    '<path class="d" d="M5.2 14.4v-2a6.8 6.8 0 0 1 13.6 0v2z"/>' +
    '<path d="M5.2 14.4v-2a6.8 6.8 0 0 1 13.6 0v2z"/>' +
    '<path d="M3.6 17.2c0-1.5 1.2-2.8 2.8-2.8h13.2v2.8z"/>' +
    '<circle class="a" cx="12" cy="6.4" r=".9"/>' },

  { k: 'ring', n: '饰品', g: 'wear', v:
    '<path class="d" d="M9.4 6.4h5.2L12 9.8z"/>' +
    '<path d="M9.4 6.4h5.2L12 9.8z"/>' +
    '<path d="M9.4 6.4 12 3.8l2.6 2.6"/>' +
    '<circle cx="12" cy="14.2" r="5.6"/>' +
    '<path d="M12 8.6v1.2"/>' },

  { k: 'dress', n: '裙装', g: 'wear', v:
    '<path class="d" d="M7.8 14.6h8.4l.4 1.8H7.4z"/>' +
    '<path d="M9.3 4.2h5.4l1 4.2 2.8 3-1.2 2.4H6.7l-1.2-2.4 2.8-3z"/>' +
    '<path d="M8.2 13.8 6.6 19.6a1.4 1.4 0 0 0 1.4 1.8h8a1.4 1.4 0 0 0 1.4-1.8l-1.6-5.8"/>' +
    '<path d="M9.3 4.2 10 2.6M14.7 4.2 14 2.6"/>' },

  { k: 'perfume', n: '香水', g: 'wear', v:
    '<rect class="d" x="6.6" y="9" width="10.8" height="12.2" rx="2.4"/>' +
    '<rect x="6.6" y="9" width="10.8" height="12.2" rx="2.4"/>' +
    '<path d="M10.4 9V5.4h3.2V9"/>' +
    '<path d="M13.6 6.6h1.6"/>' +
    '<circle class="a" cx="17.2" cy="5" r=".8"/>' +
    '<circle class="a" cx="19" cy="7.2" r=".8"/>' +
    '<path d="M9.6 14.2h4.8"/>' },

  /* ---------- 出行 · 补充 ---------- */
  { k: 'ship', n: '船舶', g: 'move', v:
    '<path class="d" d="M3.6 14.4h16.8l-2.1 4.4a2.2 2.2 0 0 1-2 1.3H7.7a2.2 2.2 0 0 1-2-1.3z"/>' +
    '<path d="M3.6 14.4h16.8l-2.1 4.4a2.2 2.2 0 0 1-2 1.3H7.7a2.2 2.2 0 0 1-2-1.3z"/>' +
    '<rect x="8.6" y="9" width="6.8" height="3.4" rx=".8"/>' +
    '<path d="M12 9V4.6M12 4.6h3.4"/>' +
    '<path d="M2.6 21.4c1.1.9 2.2.9 3.3 0s2.2-.9 3.3 0 2.2.9 3.3 0 2.2-.9 3.3 0 2.2.9 3.3 0"/>' },

  { k: 'luggage', n: '行李箱', g: 'move', v:
    '<rect class="d" x="5.4" y="6.4" width="13.2" height="13.8" rx="2.4"/>' +
    '<rect x="5.4" y="6.4" width="13.2" height="13.8" rx="2.4"/>' +
    '<path d="M9.4 6.4V4.4a1.4 1.4 0 0 1 1.4-1.4h2.4a1.4 1.4 0 0 1 1.4 1.4v2"/>' +
    '<path d="M9.2 6.4v13.8M14.8 6.4v13.8"/>' +
    '<path d="M9 20.2v1.2M15 20.2v1.2"/>' },

  { k: 'helmet', n: '头盔', g: 'move', v:
    '<path class="d" d="M3.6 13a8.4 8.4 0 0 1 16.8 0v2.4a1.8 1.8 0 0 1-1.8 1.8H5.4a1.8 1.8 0 0 1-1.8-1.8z"/>' +
    '<path d="M3.6 13a8.4 8.4 0 0 1 16.8 0v2.4a1.8 1.8 0 0 1-1.8 1.8H5.4a1.8 1.8 0 0 1-1.8-1.8z"/>' +
    '<path d="M13.2 8.4h2.6a3.6 3.6 0 0 1 3.6 3.6v1.4h-6.2z"/>' +
    '<path d="M3.6 13.8h5"/>' },

  { k: 'scooter', n: '滑板车', g: 'move', v:
    '<circle cx="6" cy="17.8" r="2.3"/>' +
    '<circle cx="17.6" cy="17.8" r="2.3"/>' +
    '<path d="M8.3 17.8h6.2"/>' +
    '<path d="M15 17.6 17.5 6.2"/>' +
    '<path d="M15.3 6.2h4.5"/>' },

  /* ---------- 运动 · 补充 ---------- */
  { k: 'swim', n: '游泳', g: 'sport', v:
    '<circle class="a" cx="16.8" cy="7.2" r="2.3"/>' +
    '<path d="M3.4 12.4c3.6 0 5.4-3 9.2-3 1.9 0 3.3.7 4.6 1.7"/>' +
    '<path d="M3 16.6c1.2 1 2.4 1 3.6 0s2.4-1 3.6 0 2.4 1 3.6 0 2.4-1 3.6 0 2.4 1 3.6 0"/>' +
    '<path d="M3 20.2c1.2 1 2.4 1 3.6 0s2.4-1 3.6 0 2.4 1 3.6 0 2.4-1 3.6 0 2.4 1 3.6 0"/>' },

  { k: 'racket', n: '球拍', g: 'sport', v:
    '<ellipse class="d" cx="10.4" cy="8.4" rx="4.9" ry="5.9"/>' +
    '<ellipse cx="10.4" cy="8.4" rx="4.9" ry="5.9"/>' +
    '<path d="M8.2 3.4v10M12.6 3.4v10M6.2 5.6h8.4M6.2 11.2h8.4"/>' +
    '<path d="m14.4 12.4 5 5"/>' +
    '<path d="m18.2 18.6 1.2-1.2"/>' },

  { k: 'fishing', n: '钓鱼', g: 'sport', v:
    '<path d="M3.4 20.4 19.8 3.4"/>' +
    '<circle class="a" cx="8.2" cy="15.6" r="1.4"/>' +
    '<path d="M19.8 3.4c.4 3.4-.5 6.2-3.2 7.8"/>' +
    '<path d="M16.6 11.2v3.6a1.9 1.9 0 0 1-3.8 0v-.9"/>' +
    '<path d="M3.4 20.4c1.2 1 2.4 1 3.6 0s2.4-1 3.6 0 2.4 1 3.6 0 2.4-1 3.6 0"/>' },

  { k: 'yoga', n: '瑜伽', g: 'sport', v:
    '<rect class="d" x="2.8" y="17" width="13.6" height="2.8" rx="1.4"/>' +
    '<rect x="2.8" y="17" width="13.6" height="2.8" rx="1.4"/>' +
    '<circle cx="19.2" cy="18.4" r="2.4"/>' +
    '<circle class="a" cx="19.2" cy="18.4" r=".9"/>' },

  /* ---------- 娱乐 · 补充 ---------- */
  { k: 'guitar', n: '吉他', g: 'fun', v:
    '<circle class="d" cx="12" cy="15.8" r="4.6"/>' +
    '<circle cx="12" cy="15.8" r="4.6"/>' +
    '<circle cx="12" cy="9.8" r="3.1"/>' +
    '<path d="M12 6.7V2.6"/>' +
    '<path d="M10.6 2.6h2.8"/>' +
    '<circle class="a" cx="12" cy="15.8" r="1.5"/>' },

  { k: 'piano', n: '钢琴', g: 'fun', v:
    '<rect class="d" x="3" y="5.4" width="18" height="13.2" rx="2.2"/>' +
    '<rect x="3" y="5.4" width="18" height="13.2" rx="2.2"/>' +
    '<path d="M7.5 5.4v13.2M12 5.4v13.2M16.5 5.4v13.2"/>' +
    '<rect class="a" x="6.1" y="5.4" width="2" height="6.2" rx=".4"/>' +
    '<rect class="a" x="10.6" y="5.4" width="2" height="6.2" rx=".4"/>' +
    '<rect class="a" x="15.1" y="5.4" width="2" height="6.2" rx=".4"/>' },

  { k: 'mic', n: '话筒', g: 'fun', v:
    '<rect class="d" x="9.2" y="2.8" width="5.6" height="10.6" rx="2.8"/>' +
    '<rect x="9.2" y="2.8" width="5.6" height="10.6" rx="2.8"/>' +
    '<path d="M5.8 11.4a6.2 6.2 0 0 0 12.4 0"/>' +
    '<path d="M12 17.6v3"/>' +
    '<path d="M9.2 20.6h5.6"/>' },

  { k: 'toy', n: '玩具', g: 'fun', v:
    '<rect class="d" x="3.8" y="10.6" width="16.4" height="9" rx="1.8"/>' +
    '<rect x="3.8" y="10.6" width="16.4" height="9" rx="1.8"/>' +
    '<path d="M7.2 10.6V7.4a1 1 0 0 1 1-1h1.6a1 1 0 0 1 1 1v3.2"/>' +
    '<path d="M13.2 10.6V7.4a1 1 0 0 1 1-1h1.6a1 1 0 0 1 1 1v3.2"/>' +
    '<circle class="a" cx="12" cy="15.1" r="1.6"/>' },

  { k: 'paw', n: '宠物', g: 'fun', v:
    '<path class="d" d="M12 11.4c3 0 5.4 2.1 5.4 4.7 0 2-1.5 3.3-3.3 3.3-.9 0-1.4-.4-2.1-.4s-1.2.4-2.1.4c-1.8 0-3.3-1.3-3.3-3.3 0-2.6 2.4-4.7 5.4-4.7z"/>' +
    '<path d="M12 11.4c3 0 5.4 2.1 5.4 4.7 0 2-1.5 3.3-3.3 3.3-.9 0-1.4-.4-2.1-.4s-1.2.4-2.1.4c-1.8 0-3.3-1.3-3.3-3.3 0-2.6 2.4-4.7 5.4-4.7z"/>' +
    '<circle class="a" cx="5.9" cy="8.6" r="1.6"/>' +
    '<circle class="a" cx="10" cy="6.1" r="1.6"/>' +
    '<circle class="a" cx="14" cy="6.1" r="1.6"/>' +
    '<circle class="a" cx="18.1" cy="8.6" r="1.6"/>' },

  /* ---------- 其他 · 补充 ---------- */
  { k: 'baby', n: '母婴', g: 'more', v:
    '<path class="d" d="M7.8 9.6h8.4l-.7 9.3a2.5 2.5 0 0 1-2.5 2.3h-2a2.5 2.5 0 0 1-2.5-2.3z"/>' +
    '<path d="M7.8 9.6h8.4l-.7 9.3a2.5 2.5 0 0 1-2.5 2.3h-2a2.5 2.5 0 0 1-2.5-2.3z"/>' +
    '<path d="M10.2 9.6c0-1.9.8-3 1.8-3s1.8 1.1 1.8 3"/>' +
    '<path d="M9.4 13h5.2"/>' +
    '<circle class="a" cx="12" cy="16.2" r="1.2"/>' },

  { k: 'tooth', n: '齿科', g: 'more', v:
    '<path class="d" d="M12 6.2c-1.2-1.1-2.8-1.6-4.3-1.2-2.4.5-3.6 2.4-3.3 4.6.2 1.6 1.1 2.5 1.6 4.3.5 1.7.6 4.7 1.8 4.7 1.5 0 .9-3.6 2.2-4.4h4c1.3.8.7 4.4 2.2 4.4 1.2 0 1.3-3 1.8-4.7.5-1.8 1.4-2.7 1.6-4.3.3-2.2-.9-4.1-3.3-4.6-1.5-.4-3.1.1-4.3 1.2z"/>' +
    '<path d="M12 6.2c-1.2-1.1-2.8-1.6-4.3-1.2-2.4.5-3.6 2.4-3.3 4.6.2 1.6 1.1 2.5 1.6 4.3.5 1.7.6 4.7 1.8 4.7 1.5 0 .9-3.6 2.2-4.4h4c1.3.8.7 4.4 2.2 4.4 1.2 0 1.3-3 1.8-4.7.5-1.8 1.4-2.7 1.6-4.3.3-2.2-.9-4.1-3.3-4.6-1.5-.4-3.1.1-4.3 1.2z"/>' +
    '<path d="M7.4 7.6c-.8.6-1.2 1.4-1.2 2.4"/>' },

  { k: 'calculator', n: '计算器', g: 'more', v:
    '<rect class="d" x="4.8" y="2.6" width="14.4" height="18.8" rx="2.6"/>' +
    '<rect x="4.8" y="2.6" width="14.4" height="18.8" rx="2.6"/>' +
    '<rect x="7.6" y="5.2" width="8.8" height="3.4" rx=".9"/>' +
    '<circle class="a" cx="8.7" cy="12.1" r=".9"/>' +
    '<circle class="a" cx="12" cy="12.1" r=".9"/>' +
    '<circle class="a" cx="15.3" cy="12.1" r=".9"/>' +
    '<circle class="a" cx="8.7" cy="15.3" r=".9"/>' +
    '<circle class="a" cx="12" cy="15.3" r=".9"/>' +
    '<circle class="a" cx="8.7" cy="18.5" r=".9"/>' +
    '<rect class="a" x="13.6" y="17.6" width="3.4" height="1.8" rx=".6"/>' },

  { k: 'pen', n: '钢笔', g: 'more', v:
    '<path d="M14.2 4.2 19.8 9.8 11.4 18.2l-6.8 1 1-6.8z"/>' +
    '<path d="M14.2 4.2h3.4c1.3 0 2.2.9 2.2 2.2v3.4z"/>' +
    '<circle class="a" cx="9.9" cy="14.1" r="1.3"/>' +
    '<path d="m9.9 14.1-4.3 4.3"/>' }
];

const ICON_BODY = {};
ICONS.forEach(ic => { ICON_BODY[ic.k] = ic.v; });

const ItemIcon = {
  props: {
    name: { type: String, default: '' },
    size: { type: Number, default: 20 },
    weight: { type: Number, default: 1.8 }
  },
  computed: {
    html() {
      const body = ICON_BODY[this.name] || '';
      if (!body) return '';
      return '<svg viewBox="0 0 24 24" width="100%" height="100%" fill="none" stroke="currentColor" '
        + 'stroke-width="' + this.weight + '" stroke-linecap="round" stroke-linejoin="round">' + body + '</svg>';
    }
  },
  template: `<span class="ic" :style="{ width: size + 'px', height: size + 'px' }" v-html="html"></span>`
};

const addMonths = (s, n) => {
  const d = parseDate(s);
  const day = d.getDate();
  d.setDate(1);
  d.setMonth(d.getMonth() + n);
  const last = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
  d.setDate(Math.min(day, last));
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/* ================= 日期选择组件 ================= */
const WEEK_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

const DateField = {
  props: {
    modelValue: { type: String, default: '' },
    allowEmpty: { type: Boolean, default: false },
    min: { type: String, default: '' },
    placeholder: { type: String, default: '选择日期' }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      open: false,
      view: '',
      weekLabels: WEEK_LABELS
    };
  },
  computed: {
    /* 购买日期默认就是今天，不再给"今天"；退役日期默认空，保留"今天"便于标记当天退役 */
    quicks() {
      const t = todayStr();
      const list = [
        { label: '昨天', get: () => addDays(t, -1) },
        { label: '一周前', get: () => addDays(t, -7) },
        { label: '一个月前', get: () => addMonths(t, -1) },
        { label: '半年前', get: () => addMonths(t, -6) },
        { label: '一年前', get: () => addMonths(t, -12) }
      ];
      if (this.allowEmpty) list.unshift({ label: '今天', get: () => t });
      return list;
    },
    today() { return todayStr(); },
    ym() { return this.view || ymOf(this.modelValue || this.today); },
    monthLabel() {
      return this.ym.slice(0, 4) + ' 年 ' + Number(this.ym.slice(5, 7)) + ' 月';
    },
    title() {
      if (!this.modelValue) return '';
      const d = parseDate(this.modelValue);
      return d.getFullYear() + ' 年 ' + (d.getMonth() + 1) + ' 月 ' + d.getDate() + ' 日';
    },
    subtitle() {
      if (!this.modelValue) return '点击选择日期';
      const n = diffDays(this.modelValue, this.today);
      const week = '周' + WEEK_LABELS[parseDate(this.modelValue).getDay()];
      let rel;
      if (n === 0) rel = '今天';
      else if (n === 1) rel = '昨天';
      else if (n > 1) rel = n + ' 天前';
      else rel = -n + ' 天后';
      return week + ' · ' + rel;
    },
    cells() {
      const first = parseDate(this.ym + '-01');
      const start = new Date(first.getFullYear(), first.getMonth(), 1 - first.getDay());
      const list = [];
      for (let i = 0; i < 42; i++) {
        const d = new Date(start.getFullYear(), start.getMonth(), start.getDate() + i);
        const s = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
        list.push({
          key: s,
          day: d.getDate(),
          out: d.getMonth() !== first.getMonth(),
          isToday: s === this.today,
          sel: s === this.modelValue,
          dis: this.isDisabled(s)
        });
      }
      return list;
    }
  },
  watch: {
    modelValue(v) { if (v) this.view = ymOf(v); },
    open(v) {
      if (!v) return;
      this.view = ymOf(this.modelValue || this.today);
      this.$nextTick(() => {
        if (this.$el && this.$el.scrollIntoView) {
          this.$el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
        }
      });
    }
  },
  methods: {
    isDisabled(s) {
      if (s > this.today) return true;
      if (this.min && s < this.min) return true;
      return false;
    },
    setVal(v) { this.$emit('update:modelValue', v); },
    quickPick(q) {
      const v = q.get();
      if (this.isDisabled(v)) return;
      this.setVal(v);
      this.view = ymOf(v);
    },
    pick(c) {
      if (c.dis) return;
      this.setVal(c.key);
      this.view = ymOf(c.key);
      this.open = false;
    },
    shiftMonth(n) { this.view = ymAdd(this.ym, n); },
    shiftYear(n) { this.view = ymAdd(this.ym, n * 12); }
  },
  template: `
  <div class="dfield" :class="{ empty: !modelValue, open: open }">
    <button type="button" class="df-card" @click="open = !open">
      <span class="df-ic">
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <rect x="3.5" y="5" width="17" height="16" rx="4.5"/>
          <path d="M8 3v4M16 3v4M3.5 10.5h17"/>
        </svg>
      </span>
      <span class="df-text">
        <b>{{ modelValue ? title : placeholder }}</b>
        <i>{{ subtitle }}</i>
      </span>
      <span class="df-chev"></span>
    </button>

    <div class="df-quick">
      <button type="button" v-for="q in quicks" :key="q.label"
        class="df-chip" :class="{ on: modelValue === q.get() }" @click="quickPick(q)">{{ q.label }}</button>
      <button type="button" v-if="allowEmpty && modelValue" class="df-chip clear" @click="setVal('')">清除</button>
    </div>

    <div class="df-panel" v-if="open">
      <div class="df-cal-head">
        <button type="button" class="df-nav year" @click="shiftYear(-1)">‹‹</button>
        <button type="button" class="df-nav" @click="shiftMonth(-1)">‹</button>
        <span class="df-month">{{ monthLabel }}</span>
        <button type="button" class="df-nav" @click="shiftMonth(1)">›</button>
        <button type="button" class="df-nav year" @click="shiftYear(1)">››</button>
      </div>
      <div class="df-week"><span v-for="w in weekLabels" :key="w">{{ w }}</span></div>
      <div class="df-grid">
        <button type="button" v-for="c in cells" :key="c.key" class="df-day"
          :class="{ out: c.out, today: c.isToday, sel: c.sel }"
          :disabled="c.dis" @click="pick(c)">{{ c.day }}</button>
      </div>
    </div>
  </div>`
};

const bridge = window.Android || {
  getState: () => localStorage.getItem('dc_state') || '',
  saveState: s => localStorage.setItem('dc_state', s),
  saveFile: (name, mime, content) => {
    const blob = new Blob([content], { type: mime });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    a.click();
  },
  setSystemBars: () => {},
  finishApp: () => {}
};

/* ================= 应用 ================= */
const app = Vue.createApp({
  /* in-DOM 模板会把 viewBox/preserveAspectRatio 小写导致失效，用指令以正确大小写回写 */
  directives: {
    viewbox: {
      mounted(el, b) { el.setAttribute('viewBox', b.value); el.setAttribute('preserveAspectRatio', 'none'); },
      updated(el, b) { el.setAttribute('viewBox', b.value); el.setAttribute('preserveAspectRatio', 'none'); }
    }
  },
  data() {
    return {
      tab: 'home',
      query: '',
      filterCat: null,
      /* 排序偏好会随数据一起保存，下次打开仍是上次选的方式与方向 */
      sortIdx: 0,
      sortDesc: true,
      sortOpen: false,
      /* 顶部视图筛选：只看当前视图，不写入存档；下次打开自动回到「全部」 */
      viewOpen: false,
      viewStatus: 'all',     // all | active | exited
      viewEarn: 'all',       // all | unearned | earned
      sorts: [
        { key: 'daily', label: '日均', desc: true,  hi: '每天成本 高 → 低', lo: '每天成本 低 → 高', hint: '按每件物品的每天成本排' },
        { key: 'price', label: '价格', desc: true,  hi: '价格 高 → 低',       lo: '价格 低 → 高',       hint: '按购入价格排' },
        { key: 'date',  label: '时间', desc: true,  hi: '购买日期 新 → 旧',   lo: '购买日期 旧 → 新',   hint: '按购买日期排' },
        { key: 'name',  label: '名称', desc: false, hi: '名称 Z → A',         lo: '名称 A → Z',         hint: '按名称拼音排' }
      ],
      items: [],
      categories: [],
      settings: { darkMode: 0,       accent: 0, currency: '¥', showRetired: true },
      /* 图表触摸取值：chart 标记当前图表，idx 是吸附后的数据点，pct/y 是浮层位置 */
      chartTouch: { chart: '', idx: -1, pct: 50, y: 0 },
      nextId: 1,
      nextCatId: 1,

      detail: null,
      sellOpen: false,
      sellPrice: '',
      sellDate: '',
      editor: null,
      batchOpen: false,
      batchSel: [],
      batchQuery: '',
      catMoveOpen: false,
      confirm: null,
      toast: '',
      toastTimer: null,
      fileMode: '',

      iconGroup: 'all',
      iconGroups: ICON_GROUPS,

      lifeEdit: false,
      lifeDraft: '',
      helpOpen: false,
      helpTab: 0,
      scrollLockY: 0,
      maskStart: null,
      maskMoved: false,
      scrollUnlockTimer: null,
      helpSections: [
        { t: '明细页顶部的三个操作', items: [
          '右上角「视图筛选」：按显示状态、回本状态、分类决定当前看哪些物品，只改变查看范围，不会改动数据',
          '搜索栏右边「排序」：点开可直接选按日均 / 价格 / 时间 / 名称排，并能切换顺序（高→低 ↔ 低→高），选择会记住',
          '「批量修改清单」在视图筛选面板底部，或设置 → 批量修改物品：进入多选模式，批量改分类 / 删除',
          '简单记：右上角管「看哪些」，排序按钮管「怎么排」，批量修改管「整份清单怎么改」'
        ]},
        { t: '视图筛选', items: [
          '显示状态：全部 / 在用 / 退环境；选「退环境」时会忽略设置里「收起退环境物品」的开关',
          '回本状态：全部 / 回本中（进度未满 100%）/ 已回本（进度满 100%，含已开始赚的）',
          '分类：与主页顶部的分类标签同步，在任意一处选择都会同时生效',
          '多个条件叠加取交集，主页会提示当前命中件数；点「清除」或面板里「重置」即可恢复',
          '筛选只影响本次查看，不会保存；导出 CSV / 备份始终是完整数据'
        ]},
        { t: '记一笔 · 添加物品', items: [
          '点右下角 ＋ 记录新物品：名称、价格、购买日期、分类',
          '「预计使用天数」是回本计算的基准，建议如实填写；详情页会按 365 天 = 1 年补一个「≈ N 年」方便对照，填写仍按天数',
          '图标可选，不选则显示名称首字；图标底色跟随分类色'
        ]},
        { t: '回本与赚（核心逻辑）', items: [
          '日均成本 = 净成本 ÷ 持有天数，持有越久每天越便宜',
          '未回本时：实心进度条 = 已用天数 ÷ 预计使用天数，从左往右成长，用满即回本 100%',
          '回本后进度条分两段：琥珀色「赚」段从最左端起步，赚的比例越大琥珀段越长（赚段:实心段 = 赚的钱:净成本），实心段是已回本的部分',
          '赚的金额 = 净成本 ÷ 预计天数 × 多用天数（售出物品按抵扣二手价后的净成本算）',
          '「已回本 +N%」= 多用天数 ÷ 预计天数，例如 +50% 表示白用了半个预计周期'
        ]},
        { t: '退环境 · 停用与出售', items: [
          '点开物品进详情页，可直接「停用」或「出售」',
          '停用/售出的物品归入「退环境物品」分类，可随时恢复回原分类',
          '出售填写二手价，从成本中抵扣，日均与赚都按净成本重算',
          '首页「总投入」是累计买入金额，不因出售而减少；有回收款时下方另列「已回收 / 净投入」两个参考数字'
        ]},
        { t: '批量修改', items: [
          '从视图筛选面板底部「批量修改清单」，或设置 → 批量修改物品进入多选模式',
          '支持搜索物品名称快速定位、全选当前列表、一键选中主页当前的筛选结果',
          '未选中任何物品时两个操作按钮都是灰色；选中后「改分类」变主题色、「删除」变红色',
          '主操作「改分类」占 4 份，高危「删除」只占 1 份，避免误触'
        ]},
        { t: '统计', items: [
          '顶部切换时间范围（全部/2年/1年/6个月/3个月/自定义）',
          '查看区间投入、每天成本走势、分类占比与 Top 5 日均排行',
          '用手指按住柱状图或折线图左右滑动，可查看对应月份/季度的金额；上下滑动页面仍然可正常滚动'
        ]},
        { t: '数据', items: [
          'CSV 导入/导出，方便在表格软件里批量整理',
          '备份/恢复包含分类与设置，换机迁移使用'
        ]}
      ],

      period: 'm12',
      customStart: '',
      customEnd: '',
      periods: [
        { key: 'all', label: '全部' },
        { key: 'm24', label: '2 年' },
        { key: 'm12', label: '1 年' },
        { key: 'm6', label: '6 个月' },
        { key: 'm3', label: '3 个月' },
        { key: 'custom', label: '自定义' }
      ],
      accents: ACCENTS,
      darkModes: ['跟随', '浅色', '深色'],
      currencies: ['¥', '$', '€', '£', '₩'],
      isDark: false
    };
  },

  computed: {
    /* 任一弹层打开时都要锁住底层页面，防止滑动穿透 */
    modalOpen() {
      return !!(this.detail || this.editor || this.batchOpen || this.sortOpen || this.viewOpen || this.confirm || this.helpOpen);
    },

    iconList() {
      return this.iconGroup === 'all' ? ICONS : ICONS.filter(i => i.g === this.iconGroup);
    },

    /* ---------- 明细 ---------- */
    activeCount() { return this.settings.showRetired ? this.items.length : this.items.filter(i => !i.retiredDate && !i.retired).length },

    totalSpent() { return this.items.reduce((s, i) => s + i.price, 0) },

    /* 二手回收款汇总：只做展示，不改「总投入」= 累计买入金额的口径 */
    totalResale() { return this.items.reduce((s, i) => s + (Number(i.resalePrice) || 0), 0) },
    netSpent() { return Math.max(0, this.totalSpent - this.totalResale) },

    dailyTotal() {
      return this.items.reduce((s, i) => s + (i.isActive === false ? 0 : i.dailyCost), 0);
    },

    monthAdded() {
      const key = todayStr().slice(0, 7);
      return this.items.filter(i => i.purchaseDate.startsWith(key)).length;
    },

    retiredHidden() {
      return this.items.filter(i => !this.isActive(i)).length;
    },

    usedCategories() {
      const includeRetired = this.settings.showRetired || this.viewStatus === 'exited';
      return this.categories.filter(c => this.items.some(i => i.categoryId === c.id && (i.isActive || includeRetired)));
    },

    /* 退环境 = 停用 + 售出，统一收进独立分类 */
    exitedItems() { return this.items.filter(i => !i.isActive) },

    showExitChip() {
      return (this.settings.showRetired || this.viewStatus === 'exited') && this.exitedItems.length > 0;
    },

    filteredItems() {
      /* 视图筛选只决定「看哪些」，仍尊重设置里的「收起退环境物品」；
         但显式选「退环境」时覆盖该开关，保证一定能看到 */
      const showR = this.settings.showRetired;
      const status = this.viewStatus;
      const exitedView = status === 'exited' || this.filterCat === 'exited';
      let list = this.items.slice();
      if (status === 'active') {
        list = list.filter(i => i.isActive);
      } else if (exitedView) {
        list = list.filter(i => !i.isActive);
      } else if (!showR) {
        list = list.filter(i => i.isActive);
      }
      /* 分类（含「退环境物品」独立视图） */
      if (this.filterCat !== null && this.filterCat !== 'exited') {
        list = list.filter(i => i.categoryId === this.filterCat);
      }
      /* 回本状态：没填预计天数（lifeProgress 为空）的归到「回本中」 */
      if (this.viewEarn === 'earned') {
        list = list.filter(i => i.lifeProgress !== null && i.lifeProgress >= 1);
      } else if (this.viewEarn === 'unearned') {
        list = list.filter(i => i.lifeProgress === null || i.lifeProgress < 1);
      }
      const q = this.query.trim().toLowerCase();
      if (q) list = list.filter(i => (i.name + ' ' + (i.note || '')).toLowerCase().includes(q));
      return list;
    },

    sortLabel() { return this.sorts[this.sortIdx].label },
    sortDirArrow() { return this.sortDesc ? '↓' : '↑' },
    /* 把抽象的方向翻译成这一档排序的实际效果，避免「名称降序」这类歧义 */
    sortStateText() {
      const s = this.sorts[this.sortIdx];
      return this.sortDesc ? s.hi : s.lo;
    },
    /* 批量弹层「选中主页筛选结果」也把视图筛选算进去 */
    filterActive() {
      return !!(this.query.trim() || this.filterCat !== null || this.viewStatus !== 'all' || this.viewEarn !== 'all');
    },
    viewScopeActive() { return this.viewStatus !== 'all' || this.viewEarn !== 'all' },
    viewStatusText() {
      return { all: '在用 + 退环境', active: '只看在用', exited: '只看退环境' }[this.viewStatus];
    },
    viewEarnText() {
      return { all: '不限回本状态', unearned: '只看未回本', earned: '只看已回本' }[this.viewEarn];
    },
    viewCatText() {
      if (this.filterCat === null) return '全部分类';
      if (this.filterCat === 'exited') return '退环境物品';
      const c = this.categories.find(x => x.id === this.filterCat);
      return c ? c.name : '未分类';
    },
    viewSummary() {
      const a = [];
      if (this.viewStatus !== 'all') a.push(this.viewStatus === 'active' ? '在用' : '退环境');
      if (this.viewEarn !== 'all') a.push(this.viewEarn === 'earned' ? '已回本' : '回本中');
      return a.join(' · ');
    },

    grouped() {
      /* 退环境物品：按离开时间倒序，单独一组展示 */
      if (this.filterCat === 'exited') {
        const list = this.filteredItems.slice().sort((a, b) => (b.retiredDate || '').localeCompare(a.retiredDate || ''));
        if (!list.length) return [];
        return [{
          id: 'exited',
          name: '退环境物品',
          color: '#94A3B8',
          items: list,
          sum: list.reduce((s, i) => s + i.price, 0)
        }];
      }

      const key = this.sorts[this.sortIdx].key;
      /* 统一写成升序比较，再按当前方向整体取反，四档排序都能双向切换 */
      const asc = {
        daily: (a, b) => a.dailyCost - b.dailyCost,
        price: (a, b) => a.price - b.price,
        date: (a, b) => a.purchaseDate.localeCompare(b.purchaseDate),
        name: (a, b) => a.name.localeCompare(b.name, 'zh')
      }[key];
      const cmp = this.sortDesc ? (a, b) => -asc(a, b) : asc;

      const groups = [];
      this.categories.forEach(c => {
        const list = this.filteredItems.filter(i => i.categoryId === c.id).sort(cmp);
        if (!list.length) return;
        groups.push({
          id: c.id,
          name: c.name,
          color: c.color,
          items: list,
          sum: list.reduce((s, i) => s + i.price, 0)
        });
      });
      const orphan = this.filteredItems.filter(i => !this.categories.some(c => c.id === i.categoryId));
      if (orphan.length) {
        groups.push({ id: -1, name: '未分类', color: '#94A3B8', items: orphan.sort(cmp), sum: orphan.reduce((s, i) => s + i.price, 0) });
      }
      if (key === 'daily' || key === 'price') {
        groups.sort((a, b) => this.sortDesc ? b.sum - a.sum : a.sum - b.sum);
      }
      return groups;
    },

    /* ---------- 统计范围 ---------- */
    range() {
      const now = todayStr().slice(0, 7);
      const earliest = this.items.length
        ? this.items.map(i => ymOf(i.purchaseDate)).sort()[0]
        : now;
      let start = now, end = now;
      switch (this.period) {
        case 'm3': start = ymAdd(now, -2); break;
        case 'm6': start = ymAdd(now, -5); break;
        case 'm12': start = ymAdd(now, -11); break;
        case 'm24': start = ymAdd(now, -23); break;
        case 'all': start = earliest > now ? now : earliest; break;
        case 'custom': start = this.customStart || ymAdd(now, -5); end = this.customEnd || now; break;
      }
      if (start > end) [start, end] = [end, start];
      const months = ymDiff(start, end) + 1;
      const fmt = ym => `${ymYear(ym)}年${ymMonth(ym)}月`;
      return {
        start, end, months,
        startMonth: ymMonth(start),
        endMonth: ymMonth(end),
        summary: this.period === 'all'
          ? `全部时间 · ${fmt(start)}起 · 共 ${months} 个月`
          : `${fmt(start)} - ${fmt(end)} · ${months} 个月`
      };
    },

    gran() {
      const m = this.range.months;
      if (m <= 15) return 'month';
      if (m <= 40) return 'quarter';
      return 'year';
    },

    granLabel() { return { month: '按月', quarter: '按季', year: '按年' }[this.gran] },
    unitWord() { return { month: '月', quarter: '季', year: '年' }[this.gran] },

    rangeItems() {
      const s = this.range.start, e = this.range.end;
      return this.items.filter(i => ymOf(i.purchaseDate) >= s && ymOf(i.purchaseDate) <= e);
    },

    rangeTotal() { return this.rangeItems.reduce((s, i) => s + i.price, 0) },

    catShare() {
      const total = this.rangeTotal;
      return this.categories.map(c => {
        const list = this.rangeItems.filter(i => i.categoryId === c.id);
        return {
          name: c.name, color: c.color, value: list.reduce((s, i) => s + i.price, 0),
          daily: list.reduce((s, i) => s + i.dailyCost, 0),
          ratio: total > 0 ? list.reduce((s, i) => s + i.price, 0) / total : 0
        };
      }).filter(x => x.value > 0).sort((a, b) => b.value - a.value);
    },

    donutArcs() {
      const C = 2 * Math.PI * 48;
      let acc = 0;
      return this.catShare.map(s => {
        const len = C * s.ratio;
        const arc = {
          color: s.color,
          dash: `${Math.max(0.5, len - 2.5)} ${C}`,
          offset: -acc
        };
        acc += len;
        return arc;
      });
    },

    buckets() {
      const { start, end } = this.range;
      const out = [];
      const crossYear = ymYear(start) !== ymYear(end);
      if (this.gran === 'month') {
        let cur = start;
        while (cur <= end) {
          const label = !crossYear
            ? `${ymMonth(cur)}月`
            : (cur === start || ymMonth(cur) === 1 ? `${ymYear(cur) % 100}年` : `${ymMonth(cur)}月`);
          out.push({ label, start: cur, end: cur });
          cur = ymAdd(cur, 1);
        }
      } else if (this.gran === 'quarter') {
        const qStart = ym => {
          const q = Math.floor((ymMonth(ym) - 1) / 3) * 3 + 1;
          return `${ymYear(ym)}-${pad(q)}`;
        };
        let cur = qStart(start);
        while (cur <= end) {
          const q = Math.floor((ymMonth(cur) - 1) / 3) + 1;
          let qe = ymAdd(cur, 2);
          if (qe > end) qe = end;
          out.push({ label: `${ymYear(cur) % 100}Q${q}`, start: cur, end: qe });
          cur = ymAdd(cur, 3);
        }
      } else {
        for (let y = ymYear(start); y <= ymYear(end); y++) {
          const s = `${y}-01`;
          let e = `${y}-12`;
          if (s < start) { /* clamp start */ }
          if (e > end) e = end;
          out.push({ label: `${y}年`, start: s < start ? start : s, end: e });
        }
      }
      return out;
    },

    bars() {
      const n = this.buckets.length;
      const every = n <= 12 ? 1 : Math.ceil(n / 12);
      return this.buckets.map((b, i) => {
        const value = this.items
          .filter(it => ymOf(it.purchaseDate) >= b.start && ymOf(it.purchaseDate) <= b.end)
          .reduce((s, it) => s + it.price, 0);
        return {
          label: b.label,
          value,
          show: every === 1 || i % every === 0 || i === n - 1
        };
      });
    },

    maxBar() { return Math.max(1, ...this.bars.map(b => b.value)) },
    peakIndex() {
      let idx = -1, max = 0;
      this.bars.forEach((b, i) => { if (b.value > max) { max = b.value; idx = i } });
      return idx;
    },
    peak() {
      const i = this.peakIndex;
      return i < 0 ? null : { label: this.bars[i].label, value: this.bars[i].value };
    },

    lineSeries() {
      const today = todayStr();
      return this.buckets.map(b => {
        const eom = ymEnd(b.end);
        const point = eom > today ? today : eom;
        return { label: b.label, value: this.dailyAtDate(point) };
      });
    },

    lineMax() { return Math.max(1, ...this.lineSeries.map(p => p.value)) },

    /* 统计页纵轴：5 等分刻度（不再只有「最大值 / 一半 / 0」），短数字格式与详情页共用 */
    lineYLabels() {
      return Array.from({ length: CHART_AXIS.steps + 1 }, (_, i) =>
        this.fmtAxis(this.lineMax * (CHART_AXIS.steps - i) / CHART_AXIS.steps));
    },
    /* 网格线与纵轴标签共用同一套 y 坐标，保证刻度与曲线严格对齐 */
    lineGridY() {
      return Array.from({ length: CHART_AXIS.steps + 1 }, (_, i) =>
        CHART_AXIS.top + CHART_AXIS.span * i / CHART_AXIS.steps);
    },
    /* 宽度按最长标签算；余量压到 3px，让曲线起点更靠左 */
    lineAxisW() {
      return Math.max(24, ...this.lineYLabels.map(s => this.estTextW(s))) + 3;
    },

    linePoints() {
      const n = this.lineSeries.length;
      // 最大值精确落在顶部刻度线，0 落在 y=120；smooth() 已钳制控制点，不会过冲出画布
      return this.lineSeries.map((p, i) => ({
        x: n === 1 ? 160 : 6 + (308 * i) / (n - 1),
        y: CHART_AXIS.bottom - (p.value / this.lineMax) * CHART_AXIS.span
      }));
    },

    linePath() { return smooth(this.linePoints, 120) },

    lineArea() {
      const pts = this.linePoints;
      if (!pts.length) return '';
      /* 必须复用 linePath：折线用 smooth(pts,120) 钳制了控制点，填充若另算一次
         smooth(pts) 会在陡升/陡降处分叉，颜色就会溢出折线之外 */
      return `${this.linePath} L${pts[pts.length - 1].x},${CHART_AXIS.bottom} L${pts[0].x},${CHART_AXIS.bottom} Z`;
    },

    /* ---------- 图表触摸取值：统计折线 / 统计柱状 / 详情折线共用一套状态 ---------- */
    chartTip() {
      const t = this.chartTouch;
      if (t.idx < 0) return null;
      if (t.chart === 'line') {
        const s = this.lineSeries[t.idx];
        return s ? { label: s.label, value: this.money(s.value), unit: '/天' } : null;
      }
      if (t.chart === 'bar') {
        const b = this.bars[t.idx];
        return b ? { label: b.label, value: this.money(b.value), unit: '' } : null;
      }
      if (t.chart === 'detail') {
        const p = this.detail && (this.detail.ptList || [])[t.idx];
        return p ? { label: `第 ${p.day} 天`, value: this.money(p.value), unit: '/天' } : null;
      }
      return null;
    },
    /* 浮层用百分比跟随容器宽度，边缘用 clamp 夹取；min(64px,30%) 保证极窄屏也不越界 */
    chartTipStyle() {
      const half = 'min(64px, 30%)';
      const t = this.chartTouch;
      return {
        left: `clamp(${half}, ${t.pct}%, calc(100% - ${half}))`,
        top: `${t.y}px`
      };
    },
    lineTouchPoint() {
      const t = this.chartTouch;
      return t.chart === 'line' && t.idx >= 0 ? (this.linePoints[t.idx] || null) : null;
    },
    detailTouchPoint() {
      const t = this.chartTouch;
      return t.chart === 'detail' && t.idx >= 0
        ? ((this.detail && this.detail.ptList || [])[t.idx] || null) : null;
    },

    lineLabels() {
      const n = this.lineSeries.length;
      const step = n <= 7 ? 1 : Math.ceil(n / 6);
      const shown = this.lineSeries
        .map((p, i) => ({
          text: p.label,
          pct: this.linePoints[i].x / 3.2,
          pos: i === 0 ? 'l' : (i === n - 1 ? 'r' : 'c')
        }))
        .filter((_, i) => i % step === 0 || i === n - 1);
      /* 相邻标签太近会重叠（如 10月/11月），保证首尾的前提下稀疏化 */
      const GAP = 9;
      const out = [];
      for (const l of shown) {
        if (out.length && l.pos !== 'r' && l.pct - out[out.length - 1].pct < GAP) continue;
        out.push(l);
      }
      if (out.length >= 2 && out[out.length - 1].pct - out[out.length - 2].pct < GAP) {
        out.splice(out.length - 2, 1);
      }
      return out;
    },

    topDaily() {
      return this.items.filter(i => i.isActive).sort((a, b) => b.dailyCost - a.dailyCost).slice(0, 5);
    },

    /* 批量列表跟随弹层内的搜索词；全选只作用于当前可见的这些物品 */
    batchItems() {
      const q = this.batchQuery.trim().toLowerCase();
      return this.items
        .filter(i => !q || (i.name + ' ' + (i.note || '')).toLowerCase().includes(q))
        .sort((a, b) => b.purchaseDate.localeCompare(a.purchaseDate));
    },
    allSelected() {
      const ids = this.batchItems.map(i => i.id);
      return ids.length > 0 && ids.every(id => this.batchSel.includes(id));
    },

    detailColor() {
      if (!this.detail) return '#64748B';
      const c = this.categories.find(x => x.id === this.detail.categoryId);
      return c ? c.color : '#64748B';
    },
    detailCat() {
      if (!this.detail) return '';
      const c = this.categories.find(x => x.id === this.detail.categoryId);
      return c ? c.name : '未分类';
    }
  },

  watch: {
    settings: {
      deep: true,
      handler() {
        this.applyTheme();
        this.persist();
      }
    },
    /* 数据范围 / 页面 / 详情一变就撤掉触摸浮层，避免旧索引落到另一组数据上 */
    period() { this.chartEnd(); },
    customStart() { this.chartEnd(); },
    customEnd() { this.chartEnd(); },
    tab() { this.chartEnd(); },
    detail() { this.chartEnd(); },
    modalOpen(v) { this.lockPageScroll(v); }
  },

  methods: {
    hexA,

    /* 弹层打开时冻结底层页面：记下滚动位置，关闭后原样还原 */
    lockPageScroll(open) {
      const html = document.documentElement;
      const body = document.body;
      if (open) {
        clearTimeout(this.scrollUnlockTimer);
        this.scrollUnlockTimer = null;
        if (html.classList.contains('modal-open')) return;
        this.scrollLockY = window.scrollY || window.pageYOffset || 0;
        body.style.top = `-${this.scrollLockY}px`;
        html.classList.add('modal-open');
      } else {
        if (!html.classList.contains('modal-open')) return;
        /* 等退出动画播完再解锁，动画期间底层依然不许滑动 */
        clearTimeout(this.scrollUnlockTimer);
        this.scrollUnlockTimer = setTimeout(() => {
          this.scrollUnlockTimer = null;
          if (this.modalOpen) return;
          html.classList.remove('modal-open');
          body.style.top = '';
          window.scrollTo(0, this.scrollLockY || 0);
        }, 320);
      }
    },

    /* 遮罩上滑动只是滑动，不能被浏览器合成的 click 误判成「点空白关闭」 */
    maskDown(e) {
      const t = e.changedTouches && e.changedTouches[0];
      this.maskStart = t ? { x: t.clientX, y: t.clientY } : null;
      this.maskMoved = false;
    },
    maskMove(e) {
      const t = e.changedTouches && e.changedTouches[0];
      if (!t || !this.maskStart) return;
      if (Math.abs(t.clientX - this.maskStart.x) > 10 ||
          Math.abs(t.clientY - this.maskStart.y) > 10) this.maskMoved = true;
    },
    maskTap() {
      const tap = !this.maskMoved;
      this.maskMoved = false;
      this.maskStart = null;
      return tap;
    },

    /* ---------- 数据加载 / 保存 ---------- */
    /* 排序偏好写进存档 / 备份；旧文件没有这两个字段时沿用默认 */
    applySortPref(root) {
      if (!root || !Number.isInteger(root.sortIdx) || root.sortIdx < 0 || root.sortIdx >= this.sorts.length) return;
      this.sortIdx = root.sortIdx;
      this.sortDesc = typeof root.sortDesc === 'boolean' ? root.sortDesc : this.sorts[root.sortIdx].desc;
    },

    load() {
      let raw = '';
      try { raw = bridge.getState() || ''; } catch (e) { raw = ''; }
      if (!raw) {
        this.categories = [
          { id: 1, name: '餐饮食品', color: '#F59E0B' },
          { id: 2, name: '服饰鞋包', color: '#8B5CF6' },
          { id: 3, name: '电子产品', color: '#3B82F6' },
          { id: 4, name: '家居日用', color: '#10B981' },
          { id: 5, name: '交通出行', color: '#64748B' },
          { id: 6, name: '医疗健康', color: '#EF4444' },
          { id: 7, name: '娱乐爱好', color: '#EC4899' },
          { id: 8, name: '其他', color: '#94A3B8' }
        ];
        this.nextCatId = 9;
        return;
      }
      try {
        const root = JSON.parse(raw);
        this.categories = (root.categories || []).map(c => ({
          id: c.id, name: c.name, color: colorOf(c.color)
        }));
        this.items = (root.items || []).map(i => this.derive({
          id: i.id,
          name: i.name,
          price: i.price,
          purchaseDate: i.date,
          categoryId: i.cat,
          note: i.note || '',
          retiredDate: i.retired || null,
          lifeDays: i.life ?? null,
          icon: i.icon || '',
          exitType: i.exit || null,
          resalePrice: i.resale ?? null
        }));
        if (root.settings) {
          this.settings = {
            darkMode: root.settings.darkMode ?? 0,
            accent: root.settings.accent ?? 0,
            currency: root.settings.currency || '¥',
            showRetired: root.settings.showRetired !== false
          };
        }
        this.applySortPref(root);
        this.nextId = root.nextId || (Math.max(0, ...this.items.map(i => i.id)) + 1);
        this.nextCatId = root.nextCatId || (Math.max(0, ...this.categories.map(c => c.id)) + 1);
      } catch (e) {
        console.error('load failed', e);
      }
    },

    derive(it) {
      const end = it.retiredDate || todayStr();
      const days = Math.max(1, diffDays(it.purchaseDate, end) + 1);
      /* 已售出按二手价抵扣：净成本分摊到持有天数，抵完为止不为负 */
      const net = Math.max(0, it.price - (it.resalePrice || 0));
      /* 回本后多用的天数视为"赚"：按购入时定的预期日均成本（净成本/预计天数）累计 */
      const extraDays = it.lifeDays ? Math.max(0, days - it.lifeDays) : 0;
      const earn = it.lifeDays ? (net / it.lifeDays) * extraDays : 0;
      /* 赚的比例 = 多用天数/预计天数 = 赚的钱/净成本 */
      const earnRate = it.lifeDays ? extraDays / it.lifeDays : 0;
      /* 进度条：单条两段，总刻度 = 净成本(回本100%) + 赚的比例（不封顶，真实比例）。
         回本段:赚段 = 净成本:赚的钱 —— 赚得少赚段就短，赚得多就长；
         赚段（琥珀）固定从最左端起步、随时间向右成长 */
      const raw = it.lifeDays ? days / it.lifeDays : null;
      let barBase = raw === null ? 0 : Math.min(raw, 1) * 100;
      let barGain = 0;
      if (raw !== null && raw > 1) {
        barBase = 100 / raw;
        barGain = (raw - 1) / raw * 100;
      }
      return {
        ...it,
        daysHeld: days,
        dailyCost: net / days,
        lifeProgress: it.lifeDays ? Math.min(1, days / it.lifeDays) : null,
        extraDays,
        earn,
        earnRate,
        barBase,
        barGain,
        isActive: !it.retiredDate
      };
    },

    persist() {
      const payload = {
        version: 2,
        sortIdx: this.sortIdx,
        sortDesc: this.sortDesc,
        categories: this.categories.map(c => ({ id: c.id, name: c.name, color: parseInt(c.color.slice(1), 16) | 0xFF000000 })),
        items: this.items.map(i => {
          const o = {
            id: i.id, name: i.name, price: i.price, date: i.purchaseDate,
            cat: i.categoryId, note: i.note || ''
          };
          if (i.retiredDate) o.retired = i.retiredDate;
          if (i.lifeDays) o.life = i.lifeDays;
          if (i.icon) o.icon = i.icon;
          if (i.exitType) o.exit = i.exitType;
          if (i.resalePrice) o.resale = i.resalePrice;
          return o;
        }),
        settings: this.settings,
        nextId: this.nextId,
        nextCatId: this.nextCatId
      };
      try { bridge.saveState(JSON.stringify(payload)); } catch (e) { console.error(e); }
    },

    /* ---------- 主题 ---------- */
    applyTheme() {
      const sysDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      const dark = this.settings.darkMode === 2 || (this.settings.darkMode === 0 && sysDark);
      this.isDark = dark;
      document.documentElement.dataset.theme = dark ? 'dark' : 'light';
      const a = ACCENTS[this.settings.accent] || ACCENTS[0];
      document.documentElement.style.setProperty('--primary', a.c);
      document.documentElement.style.setProperty('--primary-2', a.c2);
      const meta = document.querySelector('meta[name=theme-color]');
      if (meta) meta.setAttribute('content', dark ? '#080B11' : '#F2F4F7');
      try { bridge.setSystemBars(dark); } catch (e) {}
    },

    setAccent(i) { this.settings.accent = i },
    setDark(i) { this.settings.darkMode = i },
    setCurrency(c) { this.settings.currency = c },
    setShowRetired(v) {
      this.settings.showRetired = v;
      if (!v && this.filterCat === 'exited') this.filterCat = null;
      this.persist();
    },

    /* ---------- 格式化 ---------- */
    /* 预期使用天数 → 约等于多少年：365 天 = 1 年，仅用于展示，输入仍按天数 */
    lifeYears(days) {
      const d = Number(days);
      if (!d || d < 365) return '';
      const y = Math.round(d / 365 * 10) / 10;
      return Number(y.toFixed(1)) + '年';
    },
    money(v) {
      const n = Number(v) || 0;
      return this.settings.currency + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    compact(v) {
      const n = Number(v) || 0;
      if (n >= 100000000) return this.settings.currency + (n / 100000000).toFixed(2) + '亿';
      if (n >= 10000) return this.settings.currency + (n / 10000).toFixed(1) + '万';
      return this.settings.currency + Math.round(n).toLocaleString('zh-CN');
    },
    /* 图表纵轴短数字：≥1万 用「万」并保留小数，其余最多 4 位；详情页与统计页共用 */
    fmtAxis(v) {
      const n = Number(v) || 0;
      if (n >= 10000) {
        const w = n / 10000;
        const s = w >= 1000 ? Math.round(w).toString() : w >= 100 ? w.toFixed(1) : w.toFixed(2);
        /* 只去小数尾零，整数不能去（否则 1000万 会被误写成 1万） */
        return (s.includes('.') ? s.replace(/0+$/, '').replace(/\.$/, '') : s) + '万';
      }
      if (n >= 100) return String(Math.round(n));
      const s = (Math.round(n * 100) / 100).toString();
      return s.length > 4 ? String(Math.round(n * 10) / 10) : s;
    },
    /* 8.5px 字号下的标签宽度估算：中文约 9.4px，数字/符号约 5.3px。
       与 .line-yaxis / .detail-svg .ylab 的 font-size 保持一致，改字号时同步调整 */
    estTextW(s) {
      return [...s].reduce((a, ch) => a + (/[\u4e00-\u9fa5]/.test(ch) ? 9.4 : 5.3), 0);
    },
    niceDate(s) {
      if (!s) return '—';
      const [y, m, d] = s.split('-');
      return `${y}年${Number(m)}月${Number(d)}日`;
    },
    catName(id) {
      const c = this.categories.find(x => x.id === id);
      return c ? c.name : '未分类';
    },
    catCount(id) {
      if (id === 'exited') return this.exitedItems.length;
      const own = this.items.filter(i => i.categoryId === id);
      return this.settings.showRetired ? own.length : own.filter(i => i.isActive).length;
    },
    isActive(it) { return !it.retiredDate },

    /* ---------- 图表触摸取值 ----------
       手指横向位置换算到 SVG 逻辑坐标后吸附到最近的数据桶；容器宽度实时测量，
       因此同一套代码适配任意屏幕宽度。touch-action: pan-y 由 CSS 负责放行纵向滚动。 */
    chartStart(kind, e) { this.chartMove(kind, e); },
    chartMove(kind, e) {
      const t = e.touches && e.touches[0];
      const el = e.currentTarget;
      if (!t || !el) return;
      const r = el.getBoundingClientRect();
      if (!r.width) return;
      const rel = t.clientX - r.left;
      let idx = -1, pct = 50, y = 0;

      if (kind === 'line') {
        const x = rel / r.width * 320;
        const n = this.lineSeries.length;
        if (!n) return;
        idx = n === 1 ? 0 : Math.round((x - 6) / 308 * (n - 1));
        idx = Math.max(0, Math.min(n - 1, idx));
        const p = this.linePoints[idx];
        if (!p) return;
        pct = p.x / 3.2;
        /* .line-svg 高 130px 与 viewBox 的 130 为 1:1，逻辑 y 可直接作为 CSS px。
           浮层放在点上方并留 10px 间隙；点太靠顶时改放点下方，避免盖住圆点 */
        const TIP_H = 44;
        let top = p.y - TIP_H - 10;
        if (top < 2) top = p.y + 10;
        y = Math.max(2, Math.min(130 - TIP_H - 2, top));
      } else if (kind === 'bar') {
        const cols = el.querySelectorAll('.bar-col');
        if (!cols.length) return;
        let best = Infinity;
        for (let i = 0; i < cols.length; i++) {
          const cr = cols[i].getBoundingClientRect();
          const mid = (cr.left + cr.right) / 2;
          const d = Math.abs(mid - t.clientX);
          if (d < best) {
            best = d;
            idx = i;
            pct = (mid - r.left) / r.width * 100;
          }
        }
        y = 2;
      } else if (kind === 'detail') {
        const pts = (this.detail && this.detail.ptList) || [];
        if (!pts.length) return;
        const x = rel / r.width * 300;
        let best = Infinity;
        pts.forEach((p, i) => {
          const d = Math.abs(p.x - x);
          if (d < best) { best = d; idx = i; }
        });
        const p = pts[idx];
        if (!p) return;
        pct = p.x / 3;
        const TIP_H = 44;
        let top = p.y - TIP_H - 10;
        if (top < 2) top = p.y + 10;
        y = Math.max(2, Math.min(92 - TIP_H - 2, top));
      }
      if (idx < 0) return;
      this.chartTouch = { chart: kind, idx, pct, y };
    },
    chartEnd() { this.chartTouch = { chart: '', idx: -1, pct: 50, y: 0 }; },

    /* ---------- 明细交互 ---------- */
    /* 分类标签与「显示状态」共用一套视图，避免「只看在用 + 退环境分类」这类互斥组合 */
    toggleCat(id) {
      this.filterCat = this.filterCat === id ? null : id;
      if (this.filterCat === 'exited') this.viewStatus = 'exited';
      else if (this.viewStatus === 'exited') this.viewStatus = 'all';
    },
    clearCat() {
      this.filterCat = null;
      if (this.viewStatus === 'exited') this.viewStatus = 'all';
    },

    /* 顶部视图筛选：只改变查看范围，不写入存档 */
    openView() { this.viewOpen = true },
    closeView() { this.viewOpen = false },
    setViewStatus(v) {
      this.viewStatus = v;
      if (v === 'exited') this.filterCat = 'exited';
      else if (this.filterCat === 'exited') this.filterCat = null;
    },
    setViewEarn(v) { this.viewEarn = v },
    resetView() {
      this.viewStatus = 'all';
      this.viewEarn = 'all';
      this.filterCat = null;
    },
    /* 同一 tick 内关一个开一个：modalOpen 恒为 true，底层滚动锁不会中途松开 */
    openBatchFromView() {
      this.viewOpen = false;
      this.batchSel = [];
      this.batchQuery = '';
      this.catMoveOpen = false;
      this.batchOpen = true;
    },

    /* 工具栏排序按钮：可直选一档，不用连点盲循环；方向单独切换 */
    openSort() { this.sortOpen = true },
    closeSort() { this.sortOpen = false },
    setSort(i) {
      if (i === this.sortIdx) return;
      this.sortIdx = i;
      this.sortDesc = this.sorts[i].desc;   // 换档时回到这一档常用的方向
      this.persist();
    },
    setSortDesc(v) {
      if (this.sortDesc === v) return;
      this.sortDesc = v;
      this.persist();
    },

    /* ---------- 详情 ---------- */
    openDetail(item) {
      const sample = 24;
      const total = item.daysHeld;
      /* 纵轴 5 等分刻度（价格 → 0），与统计页同一口径；当前日均仍由图表标题展示 */
      const detailSteps = 4;
      const gridY = Array.from({ length: detailSteps + 1 }, (_, i) => 10 + i * 20);
      const labelY = gridY.map(y => Math.min(88, y + 3)); // 文字基线下移 3，底行不越出 92 高的画布
      const yLabels = Array.from({ length: detailSteps + 1 }, (_, i) =>
        this.fmtAxis(item.price * (detailSteps - i) / detailSteps));
      const axisX = Math.max(...yLabels.map(s => this.estTextW(s))) + 6;
      const pts = [];
      for (let i = 0; i < sample; i++) {
        const day = sample === 1 ? 1 : 1 + Math.round((total - 1) * i / (sample - 1));
        const value = item.price / Math.max(1, day);
        pts.push({
          x: axisX + (296 - axisX) * i / (sample - 1),
          y: 90 - (value / Math.max(0.01, item.price)) * 80,
          day, value
        });
      }
      const detailPath = smooth(pts, 90); // 折线与填充共用同一条路径，避免填充分叉
      const endDate = item.retiredDate || todayStr();
      this.detail = {
        ...item,
        axisX,
        gridY,
        labelY,
        yLabels,
        xStart: fmtMD(item.purchaseDate),
        xEnd: fmtMD(endDate),
        ptList: pts,
        detailLine: detailPath,
        detailArea: `${detailPath} L${pts[pts.length - 1].x},90 L${pts[0].x},90 Z`
      };
    },
    closeDetail() { this.detail = null; this.sellOpen = false; },

    dailyAt(it, back) {
      const d = addDays(todayStr(), -back);
      if (it.purchaseDate > d) return it.price;
      const end = it.retiredDate && it.retiredDate < d ? it.retiredDate : d;
      const days = Math.max(1, diffDays(it.purchaseDate, end) + 1);
      return it.price / days;
    },

    dailyAtDate(dateStr) {
      return this.items.reduce((sum, it) => {
        if (it.purchaseDate > dateStr) return sum;
        if (it.retiredDate && it.retiredDate <= dateStr) return sum;
        const days = Math.max(1, diffDays(it.purchaseDate, dateStr) + 1);
        return sum + it.price / days;
      }, 0);
    },

    /* 统一的离开/回归入口：detail 打开时就地刷新，不关弹层 */
    applyExit(item, type, date, resale) {
      const idx = this.items.findIndex(i => i.id === item.id);
      if (idx < 0) return;
      const updated = this.derive({
        ...this.items[idx],
        retiredDate: date || null,
        exitType: type || null,
        resalePrice: resale || null
      });
      this.items[idx] = updated;
      this.persist();
      if (this.detail && this.detail.id === item.id) this.openDetail(updated);
    },

    retire(item) {
      this.applyExit(item, 'retired', todayStr(), null);
      this.showToast('已停用，可在「退环境物品」里找回');
    },

    restore(item) {
      this.applyExit(item, null, null, null);
      this.showToast('已恢复在用，回到原分类');
    },

    /* ---------- 详情页直接出售 ---------- */
    openSell(item) {
      this.sellPrice = item.resalePrice ? String(item.resalePrice) : '';
      this.sellDate = item.retiredDate || todayStr();
      this.sellOpen = true;
    },
    closeSell() { this.sellOpen = false; },
    doSell() {
      const price = parseFloat(String(this.sellPrice).replace(/[^\d.]/g, ''));
      if (!price || price <= 0) return this.showToast('请输入售出价格');
      if (price > this.detail.price) return this.showToast('售出价不能高于购入价');
      const date = this.sellDate || todayStr();
      if (date < this.detail.purchaseDate) return this.showToast('售出日期不能早于购买日期');
      this.applyExit(this.detail, 'sold', date, price);
      this.sellOpen = false;
      this.showToast('已记入售出，成本已抵扣');
    },

    askDeleteItem(item) {
      this.confirm = {
        title: '删除物品',
        text: `确定删除「${item.name}」吗？此操作不可撤销。`,
        ok: '删除', danger: true,
        run: () => {
          this.items = this.items.filter(i => i.id !== item.id);
          this.detail = null;
          this.persist();
          this.showToast('已删除');
        }
      };
    },

    /* ---------- 编辑 ---------- */
    /* 详情页：点「预期使用」内联修改天数 */
    editLifeStart() {
      this.lifeEdit = true;
      this.lifeDraft = this.detail.lifeDays ? String(this.detail.lifeDays) : '';
      this.$nextTick(() => {
        if (this.$refs.lifeInput) {
          /* preventScroll：阻止浏览器聚焦时滚动页面，避免后续触点坐标漂移；
             键盘弹起（adjustResize 缩小可视区）后手动滚到输入框，保证其在键盘上方 */
          this.$refs.lifeInput.focus({ preventScroll: true });
          setTimeout(() => {
            if (this.$refs.lifeInput) {
              this.$refs.lifeInput.scrollIntoView({ block: 'center', behavior: 'smooth' });
            }
          }, 300);
        }
      });
    },
    editLifeSave() {
      const v = parseInt(this.lifeDraft, 10);
      const life = v > 0 ? v : null;
      const idx = this.items.findIndex(i => i.id === this.detail.id);
      if (idx >= 0) {
        this.items.splice(idx, 1, this.derive({ ...this.items[idx], lifeDays: life }));
        this.detail = this.items[idx];
        this.persist();
        this.showToast(life ? '预期使用已改为 ' + life + ' 天' : '已清除预期使用');
      }
      this.lifeEdit = false;
    },
    /* 详情页：点「购买日期」仅提示，不自动跳转编辑 */
    tapPurchase() {
      this.showToast('购买日期请在下方「编辑」中修改');
    },

    openEditor(item) {
      this.detail = null;
      this.lifeEdit = false;
      if (item) {
        this.editor = {
          id: item.id,
          name: item.name,
          price: String(item.price),
          date: item.purchaseDate,
          categoryId: item.categoryId,
          note: item.note || '',
          lifeDays: item.lifeDays ? String(item.lifeDays) : '',
          retiredDate: item.retiredDate || '',
          icon: item.icon || '',
          exitType: item.retiredDate ? (item.exitType === 'sold' ? 'sold' : 'retired') : 'active',
          resale: item.resalePrice ? String(item.resalePrice) : ''
        };
      } else {
        this.editor = {
          id: 0,
          name: '',
          price: '',
          date: todayStr(),
          categoryId: (this.categories[0] || {}).id,
          note: '',
          lifeDays: '',
          retiredDate: '',
          icon: '',
          exitType: 'active',
          resale: ''
        };
      }
    },
    closeEditor() { this.editor = null },

    saveEditor() {
      const e = this.editor;
      const price = parseFloat(String(e.price).replace(/[^\d.]/g, ''));
      if (!e.name.trim()) return this.showToast('请填写名称');
      if (!price || price <= 0) return this.showToast('请填写有效价格');
      if (!e.date) return this.showToast('请选择购买日期');

      const exited = e.exitType && e.exitType !== 'active';
      const payload = {
        id: e.id || this.nextId++,
        name: e.name.trim(),
        price,
        purchaseDate: e.date,
        categoryId: e.categoryId,
        note: (e.note || '').trim(),
        retiredDate: exited ? (e.retiredDate || todayStr()) : null,
        lifeDays: e.lifeDays ? parseInt(e.lifeDays, 10) || null : null,
        icon: e.icon || null,
        exitType: exited ? e.exitType : null,
        resalePrice: exited && e.exitType === 'sold'
          ? (parseFloat(String(e.resale).replace(/[^\d.]/g, '')) || null)
          : null
      };
      if (payload.retiredDate && payload.retiredDate < payload.purchaseDate) {
        return this.showToast('离开日期不能早于购买日期');
      }
      const idx = this.items.findIndex(i => i.id === payload.id);
      const derived = this.derive(payload);
      if (idx >= 0) this.items[idx] = derived;
      else this.items.unshift(derived);
      this.editor = null;
      this.persist();
      this.showToast(idx >= 0 ? '已保存' : '已记录');
    },

    addCategoryFromEditor() {
      openInputDialog(this, '新建分类', '例如 运动户外', name => {
        const cat = this.createCat(name);
        if (this.editor) this.editor.categoryId = cat.id;
      });
    },

    createCat(name) {
      const exist = this.categories.find(c => c.name.toLowerCase() === name.toLowerCase());
      if (exist) return exist;
      const palette = ['#F59E0B', '#8B5CF6', '#3B82F6', '#10B981', '#64748B', '#EF4444', '#EC4899', '#14B8A6'];
      const cat = {
        id: this.nextCatId++,
        name,
        color: palette[this.categories.length % palette.length]
      };
      this.categories.push(cat);
      this.persist();
      return cat;
    },

    /* ---------- 设置 ---------- */
    addCategoryPrompt() {
      openInputDialog(this, '新建分类', '例如 运动户外', name => {
        this.createCat(name);
        this.showToast('已添加分类');
      });
    },

    moveCat(id, delta) {
      const i = this.categories.findIndex(c => c.id === id);
      const j = i + delta;
      if (i < 0 || j < 0 || j >= this.categories.length) return;
      const list = this.categories.slice();
      const [m] = list.splice(i, 1);
      list.splice(j, 0, m);
      this.categories = list;
      this.persist();
    },

    askDeleteCat(cat) {
      const count = this.items.filter(i => i.categoryId === cat.id).length;
      if (this.categories.length <= 1) return this.showToast('至少保留一个分类');
      const fallback = this.categories.find(c => c.id !== cat.id);
      this.confirm = {
        title: '删除分类',
        text: count > 0
          ? `「${cat.name}」下还有 ${count} 件物品，删除后这些物品将移到「${fallback.name}」。`
          : `确定删除分类「${cat.name}」吗？`,
        ok: '删除', danger: true,
        run: () => {
          this.items = this.items.map(i => i.categoryId === cat.id ? { ...i, categoryId: fallback.id } : i);
          this.categories = this.categories.filter(c => c.id !== cat.id);
          if (this.filterCat === cat.id) this.filterCat = null;
          this.persist();
          this.showToast('已删除分类');
        }
      };
    },

    /* ---------- 统计周期 ---------- */
    pickPeriod(key) {
      if (key === 'custom') {
        this.customStart = this.range.start;
        this.customEnd = this.range.end;
        openCustomPicker(this, (s, e) => {
          this.customStart = s;
          this.customEnd = e;
          this.period = 'custom';
        });
        return;
      }
      this.period = key;
    },

    /* ---------- 导入导出 ---------- */
    importCsv() {
      this.fileMode = 'csv';
      this.$refs.fileInput.value = '';
      this.$refs.fileInput.click();
    },
    restoreBackup() {
      this.fileMode = 'json';
      this.$refs.fileInput.value = '';
      this.$refs.fileInput.click();
    },

    onFilePicked(ev) {
      const file = ev.target.files && ev.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = () => {
        try {
          if (this.fileMode === 'csv') this.applyCsv(String(reader.result));
          else this.applyBackup(String(reader.result));
        } catch (e) {
          this.showToast('文件解析失败');
        }
      };
      reader.readAsText(file, 'UTF-8');
    },

    applyCsv(text) {
      const rows = text.replace(/^\uFEFF/, '').split(/\r\n|\n/);
      let imported = 0, skipped = 0;
      rows.forEach(line => {
        line = line.trim();
        if (!line) return;
        const cols = splitCsv(line);
        if (cols.length < 2) { skipped++; return; }
        const name = cols[0];
        const price = parseFloat(cols[1].replace(/[^\d.]/g, ''));
        if (!name || !price || price <= 0) { skipped++; return; }
        const date = normalizeDate(cols[2]) || todayStr();
        const catName = (cols[3] || '').trim() || '其他';
        const cat = this.createCat(catName);
        this.items.unshift(this.derive({
          id: this.nextId++, name, price, purchaseDate: date,
          categoryId: cat.id, note: '', retiredDate: null, lifeDays: null
        }));
        imported++;
      });
      this.persist();
      this.showToast(`导入 ${imported} 条${skipped ? `，跳过 ${skipped} 条` : ''}`);
    },

    applyBackup(text) {
      const root = JSON.parse(text);
      if (!root.items) throw new Error('bad backup');
      this.items = (root.items || []).map(i => this.derive({
        id: i.id, name: i.name, price: i.price, purchaseDate: i.date,
        categoryId: i.cat, note: i.note || '', retiredDate: i.retired || null, lifeDays: i.life ?? null,
        icon: i.icon || '', exitType: i.exit || null, resalePrice: i.resale ?? null
      }));
      if (root.categories) this.categories = root.categories.map(c => ({ id: c.id, name: c.name, color: colorOf(c.color) }));
      if (root.settings) this.settings = { ...this.settings, ...root.settings };
      this.applySortPref(root);
      this.nextId = root.nextId || Math.max(0, ...this.items.map(i => i.id)) + 1;
      this.nextCatId = root.nextCatId || Math.max(0, ...this.categories.map(c => c.id)) + 1;
      this.persist();
      this.showToast(`已恢复 ${this.items.length} 件物品`);
    },

    exportCsv() {
      const rows = ['名称,价格,购买日期,分类,备注,停用日期,预期天数'];
      this.items.slice().sort((a, b) => a.purchaseDate.localeCompare(b.purchaseDate)).forEach(i => {
        rows.push([
          csvCell(i.name),
          i.price.toFixed(2),
          i.purchaseDate,
          csvCell(this.catName(i.categoryId)),
          csvCell(i.note || ''),
          i.retiredDate || '',
          i.lifeDays || ''
        ].join(','));
      });
      bridge.saveFile(`物尽其用-${todayStr()}.csv`, 'text/csv', '\uFEFF' + rows.join('\n'));
      this.showToast('已导出 CSV');
    },

    exportBackup() {
      const payload = {
        version: 2,
        sortIdx: this.sortIdx,
        sortDesc: this.sortDesc,
        categories: this.categories.map(c => ({ id: c.id, name: c.name, color: parseInt(c.color.slice(1), 16) | 0xFF000000 })),
        items: this.items.map(i => {
          const o = { id: i.id, name: i.name, price: i.price, date: i.purchaseDate, cat: i.categoryId, note: i.note || '' };
          if (i.retiredDate) o.retired = i.retiredDate;
          if (i.lifeDays) o.life = i.lifeDays;
          if (i.icon) o.icon = i.icon;
          if (i.exitType) o.exit = i.exitType;
          if (i.resalePrice) o.resale = i.resalePrice;
          return o;
        }),
        settings: this.settings,
        nextId: this.nextId,
        nextCatId: this.nextCatId
      };
      bridge.saveFile(`物尽其用-备份-${todayStr()}.json`, 'application/json', JSON.stringify(payload, null, 2));
      this.showToast('已导出备份');
    },

    /* ---------- 批量删除 ---------- */
    catColorOf(id) {
      const c = this.categories.find(x => x.id === id);
      return c ? c.color : '#64748B';
    },
    openBatch() {
      this.batchSel = [];
      this.batchQuery = '';
      this.catMoveOpen = false;
      this.batchOpen = true;
    },
    closeBatch() {
      this.batchOpen = false;
      this.batchSel = [];
      this.batchQuery = '';
      this.catMoveOpen = false;
    },
    togglePick(id) {
      const i = this.batchSel.indexOf(id);
      if (i >= 0) this.batchSel.splice(i, 1);
      else this.batchSel.push(id);
      if (!this.batchSel.length) this.catMoveOpen = false;
    },
    /* 全选/取消只作用于弹层里当前可见（搜索后）的物品，不会误动隐藏项 */
    toggleSelectAll() {
      const ids = this.batchItems.map(i => i.id);
      const allOn = ids.length > 0 && ids.every(id => this.batchSel.includes(id));
      if (allOn) this.batchSel = this.batchSel.filter(id => !ids.includes(id));
      else this.batchSel = [...new Set([...this.batchSel, ...ids])];
      if (!this.batchSel.length) this.catMoveOpen = false;
    },
    /* 把主页当前的搜索 / 分类筛选结果一次性选中，方便整批处理 */
    selectFiltered() {
      const ids = this.filteredItems.map(i => i.id);
      if (!ids.length) { this.showToast('当前筛选结果为空'); return; }
      this.batchQuery = '';
      this.batchSel = ids;
      this.showToast(`已选中筛选结果 ${ids.length} 件`);
    },
    /* 批量改分类：选中物品整体搬到目标分类 */
    batchMove(catId) {
      const n = this.batchSel.length;
      if (!n) return;
      const cat = this.categories.find(c => c.id === catId);
      if (!cat) return;
      const sel = this.batchSel.slice();
      this.items = this.items.map(i => sel.includes(i.id) ? this.derive({ ...i, categoryId: catId }) : i);
      this.closeBatch();
      this.persist();
      this.showToast(`已将 ${n} 件移到「${cat.name}」`);
    },

    askBatchDelete() {
      const n = this.batchSel.length;
      if (!n) return;
      const names = this.items.filter(i => this.batchSel.includes(i.id)).slice(0, 3).map(i => `「${i.name}」`).join('、');
      this.confirm = {
        title: `删除 ${n} 件物品`,
        text: `将删除 ${names}${n > 3 ? ' 等' : ''}，此操作不可撤销。`,
        ok: '删除', danger: true,
        run: () => {
          const del = this.batchSel.slice();
          this.items = this.items.filter(i => !del.includes(i.id));
          this.closeBatch();
          this.persist();
          this.showToast(`已删除 ${del.length} 件`);
        }
      };
    },

    showToast(msg) {
      this.toast = msg;
      clearTimeout(this.toastTimer);
      this.toastTimer = setTimeout(() => { this.toast = ''; }, 1800);
    }
  },

  mounted() {
    this.load();
    this.applyTheme();
    this.$nextTick(() => {
      const mq = window.matchMedia('(prefers-color-scheme: dark)');
      const onChange = () => this.applyTheme();
      if (mq.addEventListener) mq.addEventListener('change', onChange);
      else mq.addListener(onChange);
    });
    // 返回键：先关弹层，没有弹层则退出
    window.hardwareBack = () => {
      if (this.confirm) { this.confirm = null; return; }
      if (this.viewOpen) { this.closeView(); return; }
      if (this.sortOpen) { this.closeSort(); return; }
      if (this.batchOpen) { this.closeBatch(); return; }
      if (this.editor) { this.editor = null; return; }
      if (this.detail) { this.detail = null; return; }
      if (this.helpOpen) { this.helpOpen = false; return; }
      if (this.tab !== 'home') { this.tab = 'home'; return; }
      bridge.finishApp();
    };
  }
});
app.component('date-field', DateField);
app.component('item-icon', ItemIcon);
app.mount('#app');

/* ================= 工具函数 ================= */
function fmtMD(s) {
  const [, m, d] = s.split('-');
  return `${Number(m)}/${Number(d)}`;
}

function smooth(pts, yBase) {
  if (!pts || !pts.length) return '';
  if (pts.length === 1) return `M${pts[0].x},${pts[0].y}`;
  let d = `M${pts[0].x.toFixed(2)},${pts[0].y.toFixed(2)}`;
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i - 1] || pts[i];
    const p1 = pts[i];
    const p2 = pts[i + 1];
    const p3 = pts[i + 2] || p2;
    let c1x = p1.x + (p2.x - p0.x) / 6;
    let c1y = p1.y + (p2.y - p0.y) / 6;
    let c2x = p2.x - (p3.x - p1.x) / 6;
    let c2y = p2.y - (p3.y - p1.y) / 6;
    /* 钳制控制点，避免平滑过冲沉到 0 基线以下（或越过顶部） */
    if (yBase != null) {
      c1y = Math.min(Math.max(c1y, 0), yBase);
      c2y = Math.min(Math.max(c2y, 0), yBase);
    }
    d += ` C${c1x.toFixed(2)},${c1y.toFixed(2)} ${c2x.toFixed(2)},${c2y.toFixed(2)} ${p2.x.toFixed(2)},${p2.y.toFixed(2)}`;
  }
  return d;
}

function splitCsv(line) {
  const out = [];
  let cur = '', inQuotes = false;
  for (const ch of line) {
    if (ch === '"') inQuotes = !inQuotes;
    else if ((ch === ',' || ch === '，') && !inQuotes) { out.push(cur.trim()); cur = ''; }
    else cur += ch;
  }
  out.push(cur.trim());
  return out;
}

function csvCell(s) {
  if (/[",\n]/.test(s)) return '"' + s.replace(/"/g, '""') + '"';
  return s;
}

function normalizeDate(v) {
  if (!v) return '';
  const s = v.trim();
  let m = s.match(/^(\d{4})[-/.年](\d{1,2})[-/.月](\d{1,2})/);
  if (m) return `${m[1]}-${pad(Number(m[2]))}-${pad(Number(m[3]))}`;
  m = s.match(/^(\d{4})(\d{2})(\d{2})$/);
  if (m) return `${m[1]}-${m[2]}-${m[3]}`;
  return '';
}

/* 通用文本输入对话框（WebView 不支持 prompt） */
function openInputDialog(vm, title, placeholder, onOk) {
  const mask = document.createElement('div');
  mask.className = 'dialog-mask';
  mask.innerHTML = `
    <div class="dialog" style="max-width:340px">
      <h3>${title}</h3>
      <input id="dlg-input" placeholder="${placeholder}" maxlength="20"
        style="width:100%;margin-top:14px;height:44px;padding:0 13px;border-radius:13px;
        border:1px solid var(--line);background:var(--surface-2);color:var(--text);
        font-size:15px;outline:none">
      <div class="dialog-actions" style="margin-top:16px">
        <button class="text-btn" data-act="cancel">取消</button>
        <button class="text-btn strong" data-act="ok">确定</button>
      </div>
    </div>`;
  document.body.appendChild(mask);
  const input = mask.querySelector('#dlg-input');
  setTimeout(() => input.focus(), 60);

  const ok = () => {
    const v = input.value.trim();
    if (!v) return;
    mask.remove();
    onOk(v);
  };
  input.addEventListener('keydown', e => { if (e.key === 'Enter') ok(); });
  mask.addEventListener('click', ev => {
    const t = ev.target.closest('button');
    if (ev.target === mask || (t && t.dataset.act === 'cancel')) { mask.remove(); return; }
    if (t && t.dataset.act === 'ok') ok();
  });
}

/* 自定义月份区间选择（轻量面板，不依赖 dialog） */
function openCustomPicker(vm, onDone) {
  const now = new Date();
  const [sy, sm] = (vm.customStart || `${now.getFullYear()}-${pad(now.getMonth() + 1)}`).split('-').map(Number);
  const [ey, em] = (vm.customEnd || `${now.getFullYear()}-${pad(now.getMonth() + 1)}`).split('-').map(Number);

  const mask = document.createElement('div');
  mask.className = 'dialog-mask';
  mask.innerHTML = `
    <div class="dialog" style="max-width:360px">
      <h3 style="margin-bottom:14px">选择月份区间</h3>
      <div id="picker-body"></div>
      <div class="dialog-actions" style="margin-top:16px">
        <button class="text-btn" data-act="cancel">取消</button>
        <button class="text-btn strong" data-act="ok">确定</button>
      </div>
    </div>`;
  document.body.appendChild(mask);

  const state = { sy, sm, ey, em };
  const body = mask.querySelector('#picker-body');

  const render = () => {
    const row = (label, y, m, key) => `
      <div style="margin-bottom:14px">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:7px">
          <span style="font-size:12px;color:var(--text-2)">${label}</span>
          <div style="display:flex;align-items:center;gap:6px">
            <button data-y="${key}:-1" style="width:26px;height:26px;border-radius:8px;background:var(--surface-2)">‹</button>
            <b style="font-size:13px">${y} 年</b>
            <button data-y="${key}:1" style="width:26px;height:26px;border-radius:8px;background:var(--surface-2)">›</button>
          </div>
        </div>
        <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:4px">
          ${Array.from({ length: 12 }, (_, i) => i + 1).map(mm => `
            <button data-m="${key}:${mm}" style="height:30px;border-radius:8px;font-size:11px;
              background:${mm === m ? 'var(--primary)' : 'var(--surface-2)'};
              color:${mm === m ? '#fff' : 'var(--text-2)'};
              font-weight:${mm === m ? 700 : 400}">${mm}</button>`).join('')}
        </div>
      </div>`;
    body.innerHTML = row('起始', state.sy, state.sm, 's') + row('结束', state.ey, state.em, 'e');
  };
  render();

  mask.addEventListener('click', ev => {
    const t = ev.target.closest('button');
    if (ev.target === mask) { mask.remove(); return; }
    if (!t) return;
    if (t.dataset.act === 'cancel') { mask.remove(); return; }
    if (t.dataset.act === 'ok') {
      const s = `${state.sy}-${pad(state.sm)}`;
      const e = `${state.ey}-${pad(state.em)}`;
      if (s > e) { vm.showToast('起始月份不能晚于结束'); return; }
      mask.remove();
      onDone(s, e);
      return;
    }
    if (t.dataset.y) {
      const [k, d] = t.dataset.y.split(':');
      if (k === 's') state.sy = Math.max(2000, Math.min(now.getFullYear(), state.sy + Number(d)));
      else state.ey = Math.max(2000, Math.min(now.getFullYear(), state.ey + Number(d)));
      render();
      return;
    }
    if (t.dataset.m) {
      const [k, m] = t.dataset.m.split(':');
      if (k === 's') state.sm = Number(m); else state.em = Number(m);
      render();
    }
  });
}
