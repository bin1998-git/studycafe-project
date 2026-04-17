package com.tenco.view;

import com.tenco.member.MemberDTO;
import com.tenco.member.MemberService;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.seat.SeatDTO;
import com.tenco.seat.SeatService;
import com.tenco.seat.SeatType;
import com.tenco.seat.Status;
import com.tenco.ticket.TicketDTO;
import com.tenco.ticket.TicketService;
import com.tenco.ticket.TicketType;
import com.tenco.payment.PaymentDTO;
import com.tenco.payment.PaymentService;
import com.tenco.notification.NotificationDTO;
import com.tenco.notification.NotificationService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * StudyCafe Admin Dashboard
 * v0.app 다크 테마 스타일의 Swing UI
 *
 * 구조:
 *  - 좌측 사이드바(네비게이션)
 *  - 상단 TopBar (제목 + 시계)
 *  - CardLayout 기반 페이지 전환:
 *    dashboard / seats / members / tickets / payments / notifications
 */
public class StudyCafeView extends JFrame {

    // ──────────────────────────────────────────
    //  Color Palette  (v0.app dark theme)
    // ──────────────────────────────────────────
    private static final Color BG_MAIN    = new Color(9,   11,  20);
    private static final Color BG_SIDEBAR = new Color(13,  17,  30);
    private static final Color BG_CARD    = new Color(20,  26,  46);
    private static final Color BG_CARD2   = new Color(26,  33,  56);
    private static final Color BG_ROW_ALT = new Color(23,  29,  50);
    private static final Color ACCENT     = new Color(99,  102, 241);
    private static final Color GREEN      = new Color(16,  185, 129);
    private static final Color RED        = new Color(239,  68,  68);
    private static final Color YELLOW     = new Color(245, 158,  11);
    private static final Color TEXT_PRI   = new Color(241, 245, 249);
    private static final Color TEXT_SEC   = new Color(148, 163, 184);
    private static final Color BORDER     = new Color(44,   55,  80);

    // ──────────────────────────────────────────
    //  Fonts
    // ──────────────────────────────────────────
    private static final Font F_TITLE = new Font("Malgun Gothic", Font.BOLD,  22);
    private static final Font F_CARD  = new Font("Malgun Gothic", Font.BOLD,  14);
    private static final Font F_NAV   = new Font("Malgun Gothic", Font.BOLD,  13);
    private static final Font F_BODY  = new Font("Malgun Gothic", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Malgun Gothic", Font.PLAIN, 11);
    private static final Font F_NUM   = new Font("Malgun Gothic", Font.BOLD,  28);

    // ──────────────────────────────────────────
    //  State
    // ──────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JButton[]  navBtns;
    private JLabel     pageTitle;
    private JLabel     clockLabel;

    // Services
    private final MemberService memberService = new MemberService();
    private final SeatService seatService = new SeatService();
    private final TicketService ticketService = new TicketService();
    private final PaymentService paymentService = new PaymentService();
    private final NotificationService notificationService = new NotificationService();

    // 테이블/카드 캐시 (CRUD 후 갱신용)
    private JTable memberTable;
    private JTable seatTable;
    private JTable ticketTable;
    private JTable paymentTable;
    private JPanel seatMapHolder;      // 좌석 배치도 카드 내용 영역
    private JPanel miniSeatMapHolder;  // 대시보드 미니 좌석맵 영역
    private JLabel statTotalSeat;
    private JLabel statInUse;
    private JLabel statTotalMember;
    private JLabel statTodayRevenue;
    private JLabel statMonthRevenue;
    private JLabel statTotalPayments;

    private static final NumberFormat KRW = NumberFormat.getNumberInstance(Locale.KOREA);

    private static final String[] PAGE_IDS = {
        "dashboard", "seats", "members", "tickets", "payments", "notifications"
    };
    private static final String[] PAGE_TITLES = {
        "대시보드", "좌석 관리", "회원 관리", "이용권 관리", "결제 내역", "알림 센터"
    };
    private static final String[] NAV_LABELS = {
        "📊  대시보드", "🪑  좌석 관리", "👥  회원 관리",
        "🎫  이용권",   "💳  결제 내역", "🔔  알림"
    };

    // ══════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════
    public StudyCafeView() {
        setTitle("StudyCafe Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        add(buildSidebar(),  BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        // Start clock timer
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> refreshClock());
        clockTimer.start();
    }

    // ══════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_SIDEBAR,
                                              0, getHeight(), new Color(10, 13, 25)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(236, 0));

        // ── Logo ──
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        logo.setOpaque(false);
        JLabel icon = new JLabel("☕");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JPanel logoTxt = new JPanel();
        logoTxt.setOpaque(false);
        logoTxt.setLayout(new BoxLayout(logoTxt, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("StudyCafe");
        l1.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        l1.setForeground(TEXT_PRI);
        JLabel l2 = new JLabel("Admin Dashboard");
        l2.setFont(F_SMALL);
        l2.setForeground(ACCENT);
        logoTxt.add(l1);
        logoTxt.add(l2);
        logo.add(icon);
        logo.add(logoTxt);

        // ── Nav ──
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(8, 10, 8, 10));
        navBtns = new JButton[PAGE_IDS.length];
        for (int i = 0; i < PAGE_IDS.length; i++) {
            final int idx = i;
            JButton btn = makeNavBtn(NAV_LABELS[i], i == 0);
            btn.addActionListener(e -> switchPage(idx));
            navBtns[i] = btn;
            nav.add(btn);
            nav.add(Box.createVerticalStrut(3));
        }

        // ── Bottom user card ──
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 10, 14, 10));
        JPanel uc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        uc.setOpaque(false);
        JLabel av = new JLabel("👤");
        av.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JPanel ui = new JPanel();
        ui.setOpaque(false);
        ui.setLayout(new BoxLayout(ui, BoxLayout.Y_AXIS));
        JLabel un = new JLabel("관리자");
        un.setFont(F_NAV);
        un.setForeground(TEXT_PRI);
        JLabel ur = new JLabel("Administrator");
        ur.setFont(F_SMALL);
        ur.setForeground(ACCENT);
        ui.add(un);
        ui.add(ur);
        uc.add(av);
        uc.add(ui);
        bottom.add(uc);

        sidebar.add(logo,   BorderLayout.NORTH);
        sidebar.add(nav,    BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton makeNavBtn(String label, boolean active) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean a = Boolean.TRUE.equals(getClientProperty("active"));
                if (a) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(99, 102, 241, 160),
                                                  getWidth(), 0, new Color(139, 92, 246, 80)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 8, 3, getHeight() - 16, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.putClientProperty("active", active);
        btn.setFont(F_NAV);
        btn.setForeground(active ? TEXT_PRI : TEXT_SEC);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(216, 42));
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));
        return btn;
    }

    private void switchPage(int idx) {
        cardLayout.show(contentPanel, PAGE_IDS[idx]);
        pageTitle.setText(PAGE_TITLES[idx]);
        for (int i = 0; i < navBtns.length; i++) {
            boolean a = (i == idx);
            navBtns[i].putClientProperty("active", a);
            navBtns[i].setForeground(a ? TEXT_PRI : TEXT_SEC);
            navBtns[i].repaint();
        }
    }

    // ══════════════════════════════════════════
    //  MAIN AREA
    // ══════════════════════════════════════════
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_MAIN);
        main.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_MAIN);
        contentPanel.add(buildDashboard(),     "dashboard");
        contentPanel.add(buildSeats(),         "seats");
        contentPanel.add(buildMembers(),       "members");
        contentPanel.add(buildTickets(),       "tickets");
        contentPanel.add(buildPayments(),      "payments");
        contentPanel.add(buildNotifications(), "notifications");

        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_SIDEBAR);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));

        pageTitle = new JLabel("대시보드");
        pageTitle.setFont(F_TITLE);
        pageTitle.setForeground(TEXT_PRI);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GREEN);
                g2.fillOval(2, 3, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 16));

        JLabel sys = new JLabel("시스템 정상");
        sys.setFont(F_SMALL);
        sys.setForeground(GREEN);

        clockLabel = new JLabel();
        clockLabel.setFont(F_BODY);
        clockLabel.setForeground(TEXT_SEC);

        right.add(dot);
        right.add(sys);
        right.add(Box.createHorizontalStrut(8));
        right.add(clockLabel);
        bar.add(pageTitle, BorderLayout.WEST);
        bar.add(right,     BorderLayout.EAST);
        return bar;
    }

    private void refreshClock() {
        if (clockLabel != null) {
            clockLabel.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd  HH:mm:ss"))
            );
        }
    }

    // ══════════════════════════════════════════
    //  PAGE: DASHBOARD
    // ══════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel p = new JPanel();
        p.setBackground(BG_MAIN);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Row 1 – stat cards (DB 기반 실제 값)
        DashboardStats s = loadDashboardStats();
        JPanel row1 = row(120);
        row1.setLayout(new GridLayout(1, 4, 14, 0));

        JPanel totalSeatCard = statCard("🪑", "총 좌석",  String.valueOf(s.totalSeat),      "석", ACCENT);
        statTotalSeat = (JLabel) totalSeatCard.getClientProperty("valueLabel");
        row1.add(totalSeatCard);

        JPanel inUseCard = statCard("🔴", "사용 중",  String.valueOf(s.inUseSeat),      "석", RED);
        statInUse = (JLabel) inUseCard.getClientProperty("valueLabel");
        row1.add(inUseCard);

        JPanel memberCard = statCard("👥", "총 회원",  String.valueOf(s.totalMember),     "명", GREEN);
        statTotalMember = (JLabel) memberCard.getClientProperty("valueLabel");
        row1.add(memberCard);

        JPanel revenueCard = statCard("💳", "오늘 매출", KRW.format(s.todayRevenue), "원", YELLOW);
        statTodayRevenue = (JLabel) revenueCard.getClientProperty("valueLabel");
        row1.add(revenueCard);

        p.add(row1);
        p.add(Box.createVerticalStrut(16));

        // Row 2 – mini seat map + recent activity
        JPanel row2 = row(300);
        row2.setLayout(new GridLayout(1, 2, 14, 0));
        row2.add(buildMiniSeatMap());
        row2.add(buildRecentActivity());
        p.add(row2);
        p.add(Box.createVerticalStrut(16));

        // Row 3 – quick action buttons
         JPanel row3 = row(52);
         row3.setLayout(new GridLayout(1, 3, 14, 0));

         JButton checkInBtn = actionBtn("🚪  입실 처리",  ACCENT);
         checkInBtn.addActionListener(e -> handleCheckIn());
         row3.add(checkInBtn);

         JButton checkOutBtn = actionBtn("🚶  퇴실 처리",  new Color(22, 120, 90));
         checkOutBtn.addActionListener(e -> handleCheckOut());
         row3.add(checkOutBtn);

         JButton registerMemberBtn = actionBtn("➕  회원 등록",  new Color(80, 60, 160));
         registerMemberBtn.addActionListener(e -> showRegisterMemberDialog());
         row3.add(registerMemberBtn);

         p.add(row3);
         p.add(Box.createVerticalGlue());
         return p;
    }

    private JPanel statCard(String icon, String label, String val, String unit, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_SMALL);
        lbl.setForeground(TEXT_SEC);
        JPanel vRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        vRow.setOpaque(false);
        JLabel v = new JLabel(val);
        v.setFont(F_NUM);
        v.setForeground(TEXT_PRI);
        JLabel u = new JLabel(unit);
        u.setFont(F_BODY);
        u.setForeground(TEXT_SEC);
        vRow.add(v);
        vRow.add(u);
        txt.add(lbl);
        txt.add(vRow);

        c.add(ico, BorderLayout.EAST);
        c.add(txt, BorderLayout.CENTER);
        c.putClientProperty("valueLabel", v);
        return c;
    }

    private JPanel buildMiniSeatMap() {
        JPanel card = card("좌석 현황");
        miniSeatMapHolder = new JPanel(new BorderLayout());
        miniSeatMapHolder.setOpaque(false);
        refreshMiniSeatMap();

        JPanel content = (JPanel) card.getClientProperty("content");
        content.add(miniSeatMapHolder, BorderLayout.CENTER);
        return card;
    }

    /** DB 좌석 데이터를 읽어 미니 좌석맵 다시 그리기 */
    private void refreshMiniSeatMap() {
        if (miniSeatMapHolder == null) return;
        miniSeatMapHolder.removeAll();

        List<SeatDTO> seats = loadSeats();
        java.util.Map<Integer, SeatOccupant> occMap = loadOccupantsBySeatId();
        int n = Math.max(seats.size(), 1);
        int cols = Math.min(8, n);
        int rows = (int) Math.ceil(n / (double) cols);
        JPanel grid = new JPanel(new GridLayout(rows, cols, 5, 5));
        grid.setOpaque(false);

        for (SeatDTO seat : seats) {
            boolean used = seat.getStatus() == Status.IN_USE;
            boolean disabled = seat.getStatus() == Status.DISABLED;
            SeatOccupant occ = used ? occMap.get(seat.getSeatId()) : null;
            JPanel s = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color fill = used
                            ? new Color(239, 68, 68, 180)
                            : disabled
                                ? new Color(100, 116, 139, 160)
                                : new Color(16, 185, 129, 130);
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                    g2.dispose();
                }
            };
            s.setOpaque(false);
            String stateTxt = used ? "사용 중" : disabled ? "사용 불가" : "사용 가능";
            String tip = "<html><b>" + seat.getSeatNumber() + "</b> — " + stateTxt
                    + (occ != null ? "<br/>사용자: <b>" + occ.memberName + "</b>" : "")
                    + "</html>";
            s.setToolTipText(tip);
            grid.add(s);
        }

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        legend.setOpaque(false);
        addLegend(legend, GREEN, "사용 가능");
        addLegend(legend, RED,   "사용 중");
        addLegend(legend, new Color(100,116,139), "사용 불가");

        miniSeatMapHolder.add(grid,   BorderLayout.CENTER);
        miniSeatMapHolder.add(legend, BorderLayout.SOUTH);
        miniSeatMapHolder.revalidate();
        miniSeatMapHolder.repaint();
    }

    private JPanel buildRecentActivity() {
        JPanel card = card("최근 활동");
        String[][] rows = loadRecentActivityRows();
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (String[] r : rows) {
            JPanel item = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(255,255,255,5));
                    g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-4, 7, 7);
                    g2.dispose();
                }
            };
            item.setOpaque(false);
            item.setBorder(new EmptyBorder(5, 8, 5, 8));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

            JLabel ico = new JLabel(r[0]);
            ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            ico.setBorder(new EmptyBorder(0,0,0,8));

            JPanel mid = new JPanel(new GridLayout(1, 2, 4, 0));
            mid.setOpaque(false);
            JLabel nm = new JLabel(r[1]); nm.setFont(F_BODY); nm.setForeground(TEXT_PRI);
            JLabel dt = new JLabel(r[2]); dt.setFont(F_SMALL); dt.setForeground(ACCENT);
            mid.add(nm);
            mid.add(dt);
            JLabel tm = new JLabel(r[3]); tm.setFont(F_SMALL); tm.setForeground(TEXT_SEC);

            item.add(ico, BorderLayout.WEST);
            item.add(mid, BorderLayout.CENTER);
            item.add(tm,  BorderLayout.EAST);
            list.add(item);
            list.add(Box.createVerticalStrut(2));
        }
        JPanel content = (JPanel) card.getClientProperty("content");
        content.add(list, BorderLayout.NORTH);
        return card;
    }

    private JButton actionBtn(String label, Color color) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? color.brighter() : color;
                g2.setPaint(new GradientPaint(0, 0, c, getWidth(), getHeight(), c.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ══════════════════════════════════════════
    //  PAGE: SEATS
    // ══════════════════════════════════════════
    private JPanel buildSeats() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel toolbar = new JPanel(new BorderLayout());
         toolbar.setOpaque(false);
         toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));
         JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
         btns.setOpaque(false);

         JButton addSeatBtn = pillBtn("➕ 좌석 추가",  ACCENT);
         addSeatBtn.addActionListener(e -> showAddSeatDialog());
         btns.add(addSeatBtn);

         JButton changeSeatStatusBtn = pillBtn("🔄 상태 변경",  new Color(50,100,170));
         changeSeatStatusBtn.addActionListener(e -> showChangeSeatStatusDialog());
         btns.add(changeSeatStatusBtn);

         JButton deleteSeatBtn = pillBtn("🗑️ 삭제",       new Color(160,40,40));
         deleteSeatBtn.addActionListener(e -> showDeleteSeatDialog());
         btns.add(deleteSeatBtn);

         toolbar.add(btns, BorderLayout.EAST);
         p.add(toolbar, BorderLayout.NORTH);

        JPanel gridCard = card("전체 좌석 배치도");
        seatMapHolder = new JPanel(new BorderLayout());
        seatMapHolder.setOpaque(false);
        refreshSeatMap();

        JPanel content = (JPanel) gridCard.getClientProperty("content");
        content.add(seatMapHolder, BorderLayout.CENTER);
        p.add(gridCard, BorderLayout.CENTER);
        return p;
    }

    /** DB 좌석 데이터로 전체 좌석 배치도 다시 그리기 */
    private void refreshSeatMap() {
        if (seatMapHolder == null) return;
        seatMapHolder.removeAll();

        List<SeatDTO> seats = loadSeats();
        java.util.Map<Integer, SeatOccupant> occMap = loadOccupantsBySeatId();

        int n = Math.max(seats.size(), 1);
        int cols = Math.min(8, Math.max(n, 1));
        int rows = (int) Math.ceil(n / (double) cols);
        JPanel grid = new JPanel(new GridLayout(rows, cols, 10, 10));
        grid.setOpaque(false);

        DateTimeFormatter tfmt = DateTimeFormatter.ofPattern("HH:mm");

        for (SeatDTO seat : seats) {
            boolean used = seat.getStatus() == Status.IN_USE;
            boolean disabled = seat.getStatus() == Status.DISABLED;
            boolean prem = seat.getSeatType() == SeatType.PREMIUM;
            SeatOccupant occ = used ? occMap.get(seat.getSeatId()) : null;
            String label = used && occ != null
                    ? "<html><center>" + seat.getSeatNumber()
                      + "<br/><span style='font-size:9px'>" + occ.memberName + "</span></center></html>"
                    : seat.getSeatNumber();
            JButton sb = new JButton(label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = used
                            ? new Color(239, 68, 68, 200)
                            : disabled
                                ? new Color(100, 116, 139, 200)
                                : prem
                                    ? new Color(99, 102, 241, 160)
                                    : new Color(16, 185, 129, 140);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(255, 255, 255, 35));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            sb.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
            sb.setForeground(Color.WHITE);
            sb.setBorderPainted(false);
            sb.setContentAreaFilled(false);
            sb.setFocusPainted(false);
            sb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            String stateTxt = used ? "🔴 사용 중" : disabled ? "⚫ 사용 불가" : "🟢 사용 가능";
            StringBuilder tip = new StringBuilder("<html>");
            tip.append("<b>").append(seat.getSeatNumber()).append("</b> (ID ").append(seat.getSeatId()).append(")<br/>");
            tip.append("타입: ").append(prem ? "프리미엄" : "일반").append("<br/>");
            tip.append("구역: ").append(seat.getZone() != null ? seat.getZone() : "-").append("<br/>");
            tip.append("상태: ").append(stateTxt);
            if (occ != null) {
                tip.append("<br/>사용자: <b>").append(occ.memberName).append("</b>");
                tip.append("<br/>입실: ").append(occ.startedAt == null ? "-" : occ.startedAt.format(tfmt));
            }
            tip.append("</html>");
            sb.setToolTipText(tip.toString());
            grid.add(sb);
        }

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        legend.setOpaque(false);
        addLegend(legend, GREEN,  "일반 / 사용 가능");
        addLegend(legend, ACCENT, "프리미엄 / 사용 가능");
        addLegend(legend, RED,    "사용 중");
        addLegend(legend, new Color(100, 116, 139), "사용 불가");

        seatMapHolder.add(grid,   BorderLayout.CENTER);
        seatMapHolder.add(legend, BorderLayout.SOUTH);
        seatMapHolder.revalidate();
        seatMapHolder.repaint();
    }

    // ══════════════════════════════════════════
    //  PAGE: MEMBERS
    // ══════════════════════════════════════════
    private JPanel buildMembers() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel toolbar = new JPanel(new BorderLayout());
         toolbar.setOpaque(false);
         toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));
         JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
         btns.setOpaque(false);

         JButton registerBtn = pillBtn("➕ 회원 등록", ACCENT);
         registerBtn.addActionListener(e -> showRegisterMemberDialog());
         btns.add(registerBtn);

         JButton editBtn = pillBtn("✏️ 수정",      new Color(50,100,170));
         editBtn.addActionListener(e -> showEditMemberDialog());
         btns.add(editBtn);

         JButton deleteBtn = pillBtn("🗑️ 삭제",      new Color(160,40,40));
         deleteBtn.addActionListener(e -> showDeleteMemberDialog());
         btns.add(deleteBtn);

         JTextField searchField = searchField("🔍  이름 또는 전화번호 검색...");
         searchField.addKeyListener(new KeyAdapter() {
             @Override public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     // TODO: 회원 검색 로직 추가
                 }
             }
         });

         toolbar.add(searchField, BorderLayout.WEST);
         toolbar.add(btns, BorderLayout.EAST);
         p.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","이름","전화번호","이메일","가입일","이용권 상태"};
        memberTable = styledTable(cols, loadMemberRows());
        p.add(styledScroll(memberTable), BorderLayout.CENTER);
        return p;
    }

    /** DB 회원 목록 → 테이블 Row[][] */
    private Object[][] loadMemberRows() {
        try {
            List<MemberDTO> list = memberService.getMemberList();
            Object[][] rows = new Object[list.size()][6];
            DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < list.size(); i++) {
                MemberDTO m = list.get(i);
                rows[i][0] = m.getMemberId();
                rows[i][1] = m.getName();
                rows[i][2] = m.getPhone();
                rows[i][3] = m.getEmail();
                rows[i][4] = m.getCreatedAt() != null ? m.getCreatedAt().format(dfmt) : "-";
                rows[i][5] = summarizeMemberTickets(m.getMemberId());
            }
            return rows;
        } catch (Exception e) {
            return new Object[0][6];
        }
    }

    /** 해당 회원이 현재 보유/사용 중인 이용권을 한 줄로 요약 */
    private String summarizeMemberTickets(int memberId) {
        try {
            List<MemberTicketDTO> tickets = ticketService.getMemberTickets(memberId);
            if (tickets == null || tickets.isEmpty()) return "없음";
            MemberTicketDTO latest = tickets.get(0); // DAO 가 최신순 정렬
            TicketDTO ticket = ticketService.getTicketById(latest.getTicketId());
            StringBuilder sb = new StringBuilder(ticket.getName());
            if (ticket.getType() == TicketType.TIME) {
                int hours = Math.max(latest.getRemainingMinutes(), 0) / 60;
                int minutes = Math.max(latest.getRemainingMinutes(), 0) % 60;
                sb.append(" (잔여 ").append(hours).append("h ").append(minutes).append("m)");
            }
            sb.append(" · ").append(latest.getStatus());
            return sb.toString();
        } catch (Exception e) {
            return "-";
        }
    }

    private void refreshMemberTable() {
        if (memberTable == null) return;
        DefaultTableModel m = (DefaultTableModel) memberTable.getModel();
        m.setDataVector(loadMemberRows(),
                new Object[]{"ID","이름","전화번호","이메일","가입일","이용권 상태"});
    }

    // ══════════════════════════════════════════
    //  PAGE: TICKETS
    // ══════════════════════════════════════════
    private JPanel buildTickets() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel typeRow = row(150);
        typeRow.setLayout(new GridLayout(1, 3, 14, 0));
        String[] summaries = buildTicketPriceSummaries();
        typeRow.add(ticketTypeCard("⏱️","시간권",  summaries[0],  ACCENT));
        typeRow.add(ticketTypeCard("📅","기간권",  summaries[1],   GREEN));
        typeRow.add(ticketTypeCard("🌟","프리미엄","기간권 + 프리미엄 좌석\n" + summaries[2], YELLOW));
        p.add(typeRow, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel ttl = new JLabel("이용권 목록");
        ttl.setFont(F_CARD);
        ttl.setForeground(TEXT_PRI);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
         btns.setOpaque(false);

         JButton addTicketBtn = pillBtn("➕ 이용권 추가", ACCENT);
         addTicketBtn.addActionListener(e -> showAddTicketDialog());
         btns.add(addTicketBtn);

         JButton deleteTicketBtn = pillBtn("🗑️ 삭제",        new Color(160,40,40));
         deleteTicketBtn.addActionListener(e -> showDeleteTicketDialog());
         btns.add(deleteTicketBtn);

         toolbar.add(ttl,  BorderLayout.WEST);
         toolbar.add(btns, BorderLayout.EAST);

        String[] cols = {"ID","이용권명","타입","시간/기간","가격","상태"};
        ticketTable = styledTable(cols, loadTicketRows());
        bottom.add(toolbar, BorderLayout.NORTH);
        bottom.add(styledScroll(ticketTable), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    private Object[][] loadTicketRows() {
        try {
            List<TicketDTO> list = ticketService.getTicketList();
            Object[][] rows = new Object[list.size()][6];
            for (int i = 0; i < list.size(); i++) {
                TicketDTO t = list.get(i);
                rows[i][0] = t.getTicketId();
                rows[i][1] = t.getName();
                rows[i][2] = t.getType() != null ? t.getType().name() : "";
                rows[i][3] = t.getType() == TicketType.TIME
                        ? t.getDurationValue() + "분"
                        : t.getDurationValue() + "일";
                rows[i][4] = KRW.format(t.getPrice()) + "원";
                rows[i][5] = "판매중";
            }
            return rows;
        } catch (Exception e) {
            return new Object[0][6];
        }
    }

    private void refreshTicketTable() {
        if (ticketTable == null) return;
        DefaultTableModel m = (DefaultTableModel) ticketTable.getModel();
        m.setDataVector(loadTicketRows(),
                new Object[]{"ID","이용권명","타입","시간/기간","가격","상태"});
    }

    private JPanel ticketTypeCard(String ico, String name, String detail, Color color) {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,BG_CARD,getWidth(),getHeight(),BG_CARD2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel i = new JLabel(ico);  i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel n = new JLabel(name); n.setFont(new Font("Malgun Gothic", Font.BOLD, 15)); n.setForeground(TEXT_PRI);
        c.add(i);
        c.add(Box.createVerticalStrut(4));
        c.add(n);
        // detail 은 줄바꿈(\n) 지원 — 각 줄을 개별 라벨로 추가
        String[] lines = detail.split("\\n");
        for (String line : lines) {
            JLabel d = new JLabel(line);
            d.setFont(F_SMALL);
            d.setForeground(TEXT_SEC);
            c.add(d);
        }
        return c;
    }

    /**
     * TICKET 테이블을 읽어 시간권 / 기간권 / 프리미엄 카드에 표시할 가격 요약 문자열을 돌려준다.
     * [0] 시간권(일반)  [1] 기간권(일반)  [2] 프리미엄(기간권)
     * 현재 TICKET 테이블에는 type(TIME/PERIOD)만 있으므로,
     * - 일반 시간권     = 모든 TIME 티켓
     * - 일반 기간권     = PERIOD 중 duration_value < 30
     * - 프리미엄 기간권 = PERIOD 중 duration_value >= 30
     * 기준으로 분류한다. (데이터가 없거나 로드 실패 시에는 간단한 기본 문구 반환)
     */
    private String[] buildTicketPriceSummaries() {
        String[] def = {
                "1h / 3h / 5h / 10h",
                "7일 / 15일",
                "30일 / 90일"
        };
        try {
            List<TicketDTO> tickets = ticketService.getTicketList();
            StringBuilder timeSb = new StringBuilder();
            StringBuilder periodSb = new StringBuilder();
            StringBuilder premiumSb = new StringBuilder();
            for (TicketDTO t : tickets) {
                String line = t.getName() + " · " + KRW.format(t.getPrice()) + "원";
                if (t.getType() == TicketType.TIME) {
                    if (timeSb.length() > 0) timeSb.append("\n");
                    timeSb.append(line);
                } else if (t.getType() == TicketType.PERIOD) {
                    if (t.getDurationValue() >= 30) {
                        if (premiumSb.length() > 0) premiumSb.append("\n");
                        premiumSb.append(line);
                    } else {
                        if (periodSb.length() > 0) periodSb.append("\n");
                        periodSb.append(line);
                    }
                }
            }
            String s1 = timeSb.length()    == 0 ? def[0] : timeSb.toString();
            String s2 = periodSb.length()  == 0 ? def[1] : periodSb.toString();
            String s3 = premiumSb.length() == 0 ? def[2] : premiumSb.toString();
            return new String[]{s1, s2, s3};
        } catch (Exception e) {
            return def;
        }
    }

    // ══════════════════════════════════════════
    //  PAGE: PAYMENTS
    // ══════════════════════════════════════════
    private JPanel buildPayments() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        PaymentStats pst = loadPaymentStats();
        JPanel top = row(110);
        top.setLayout(new GridLayout(1, 3, 14, 0));

        JPanel todayCard = statCard("💰","오늘 매출", KRW.format(pst.today), "원", ACCENT);
        JPanel monthCard = statCard("📈","이번 달",    KRW.format(pst.month), "원", GREEN);
        JPanel totalCard = statCard("🧾","총 거래",    String.valueOf(pst.total), "건", YELLOW);
        // value 라벨 참조 저장 (갱신용)
        statMonthRevenue   = (JLabel) monthCard.getClientProperty("valueLabel");
        statTotalPayments  = (JLabel) totalCard.getClientProperty("valueLabel");

        top.add(todayCard);
        top.add(monthCard);
        top.add(totalCard);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
         center.setOpaque(false);
         JPanel toolbar = new JPanel(new BorderLayout());
         toolbar.setOpaque(false);
         toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

         JTextField paymentSearch = searchField("🔍  결제 검색...");
         paymentSearch.addKeyListener(new KeyAdapter() {
             @Override public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     // TODO: 결제 검색 로직 추가
                 }
             }
         });
         toolbar.add(paymentSearch, BorderLayout.WEST);

        String[] cols = {"거래 ID","회원 ID","회원명","이용권","결제금액","결제방법","결제일시","상태"};
        paymentTable = styledTable(cols, loadPaymentRows());
        center.add(toolbar, BorderLayout.NORTH);
        center.add(styledScroll(paymentTable), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private Object[][] loadPaymentRows() {
        try {
            List<PaymentDTO> list = paymentService.getPaymentList();
            if (list == null) return new Object[0][8];
            Object[][] rows = new Object[list.size()][8];
            DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (int i = 0; i < list.size(); i++) {
                PaymentDTO pay = list.get(i);
                String memberName = safeMemberName(pay.getMember_id());
                String ticketName = safeTicketName(pay.getTicket_id());
                rows[i][0] = pay.getPayment_id();
                rows[i][1] = pay.getMember_id();
                rows[i][2] = memberName;
                rows[i][3] = ticketName;
                rows[i][4] = KRW.format(pay.getAmount()) + "원";
                rows[i][5] = mapMethod(pay.getMethod());
                rows[i][6] = pay.getPaidAt() != null ? pay.getPaidAt().format(dfmt) : "-";
                rows[i][7] = mapPaymentStatus(pay.getStatus());
            }
            return rows;
        } catch (Exception e) {
            return new Object[0][8];
        }
    }

    private void refreshPaymentTable() {
        if (paymentTable == null) return;
        DefaultTableModel m = (DefaultTableModel) paymentTable.getModel();
        m.setDataVector(loadPaymentRows(),
                new Object[]{"거래 ID","회원 ID","회원명","이용권","결제금액","결제방법","결제일시","상태"});
    }

    private String safeMemberName(int memberId) {
        try { return memberService.getMemberById(memberId).getName(); }
        catch (Exception e) { return "(알 수 없음)"; }
    }

    private String safeTicketName(int ticketId) {
        try { return ticketService.getTicketById(ticketId).getName(); }
        catch (Exception e) { return "(알 수 없음)"; }
    }

    private String mapMethod(String m) {
        if (m == null) return "-";
        return switch (m.toUpperCase()) {
            case "CARD"     -> "카드";
            case "CASH"     -> "현금";
            case "TRANSFER" -> "계좌이체";
            default         -> m;
        };
    }

    private String mapPaymentStatus(String s) {
        if (s == null) return "-";
        return switch (s.toUpperCase()) {
            case "SUCCESS" -> "완료";
            case "FAIL"    -> "실패";
            case "REFUND"  -> "환불";
            default        -> s;
        };
    }

    // ══════════════════════════════════════════
    //  PAGE: NOTIFICATIONS
    // ══════════════════════════════════════════
    private JPanel buildNotifications() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel toolbar = new JPanel(new BorderLayout());
         toolbar.setOpaque(false);
         toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));
         JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
         btns.setOpaque(false);

         JButton markAllReadBtn = pillBtn("✅ 모두 읽음",  new Color(50,100,170));
         markAllReadBtn.addActionListener(e -> handleMarkAllAsRead());
         btns.add(markAllReadBtn);

         JButton deleteAllBtn = pillBtn("🗑️ 전체 삭제", new Color(160,40,40));
         deleteAllBtn.addActionListener(e -> handleDeleteAllNotifications());
         btns.add(deleteAllBtn);

         toolbar.add(btns, BorderLayout.EAST);
         p.add(toolbar, BorderLayout.NORTH);

        Object[][] notifs = loadNotificationRows();

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (Object[] n : notifs) {
            boolean unread = (boolean) n[4];
            JPanel item = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(unread ? BG_CARD2 : BG_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, 10, 10);
                    if (unread) {
                        g2.setColor(ACCENT);
                        g2.fillRoundRect(0, 8, 3, getHeight()-22, 2, 2);
                    }
                    g2.dispose();
                }
            };
            item.setOpaque(false);
            item.setBorder(new EmptyBorder(12, 16, 12, 16));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

            JLabel ico = new JLabel((String) n[0]);
            ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            ico.setBorder(new EmptyBorder(0, 4, 0, 12));

            JPanel txt = new JPanel();
            txt.setOpaque(false);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
            JLabel t = new JLabel((String) n[1]);
            t.setFont(unread ? new Font("Malgun Gothic", Font.BOLD, 13) : F_BODY);
            t.setForeground(TEXT_PRI);
            JLabel b = new JLabel((String) n[2]);
            b.setFont(F_SMALL);
            b.setForeground(TEXT_SEC);
            txt.add(t);
            txt.add(b);

            JLabel tm = new JLabel((String) n[3]);
            tm.setFont(F_SMALL);
            tm.setForeground(TEXT_SEC);
            tm.setBorder(new EmptyBorder(0, 8, 0, 0));

            item.add(ico, BorderLayout.WEST);
            item.add(txt, BorderLayout.CENTER);
            item.add(tm,  BorderLayout.EAST);
            list.add(item);
            list.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        styleScrollBar(scroll);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════

    /** 카드 컨테이너 – content 패널은 card.getClientProperty("content") 로 접근 */
    private JPanel card(String title) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_CARD);
        lbl.setForeground(TEXT_PRI);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        card.putClientProperty("content", content);
        card.add(lbl,     BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /** 높이 고정 행 패널 */
    private JPanel row(int height) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        p.setPreferredSize(new Dimension(0, height));
        return p;
    }

    private JTable styledTable(String[] cols, Object[][] data) {
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row)
                    ? new Color(99,102,241,90)
                    : (row % 2 == 0 ? BG_CARD : BG_ROW_ALT));
                c.setForeground(TEXT_PRI);
                if (c instanceof JLabel) ((JLabel)c).setBorder(new EmptyBorder(0,10,0,10));
                return c;
            }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRI);
        table.setGridColor(BORDER);
        table.setRowHeight(36);
        table.setFont(F_BODY);
        table.setSelectionBackground(new Color(99,102,241,90));
        table.setSelectionForeground(TEXT_PRI);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(new Color(14, 18, 38));
        hdr.setForeground(TEXT_SEC);
        hdr.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0, BORDER));
        hdr.setPreferredSize(new Dimension(0, 40));
        return table;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        styleScrollBar(sp);
        return sp;
    }

    private void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = BORDER;
                trackColor = BG_CARD;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    private JTextField searchField(String placeholder) {
        JTextField f = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setBackground(BG_CARD);
        f.setForeground(TEXT_SEC);
        f.setCaretColor(TEXT_PRI);
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_PRI); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(TEXT_SEC); }
            }
        });
        return f;
    }

    private JButton pillBtn(String label, Color color) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        return btn;
    }

    private void addLegend(JPanel panel, Color color, String label) {
         JPanel dot = new JPanel() {
             @Override protected void paintComponent(Graphics g) {
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g2.setColor(color);
                 g2.fillRoundRect(0, 4, 12, 12, 4, 4);
                 g2.dispose();
             }
         };
         dot.setOpaque(false);
         dot.setPreferredSize(new Dimension(14, 20));
         JLabel lbl = new JLabel(label);
         lbl.setFont(F_SMALL);
         lbl.setForeground(TEXT_SEC);
         panel.add(dot);
         panel.add(lbl);
     }

    // ══════════════════════════════════════════
    //  DB → VIEW 데이터 로더 / 통계
    // ══════════════════════════════════════════

    /** DB 좌석 전체 (실패 시 빈 리스트) */
    private List<SeatDTO> loadSeats() {
        try { return seatService.getSeatList(); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    /** 현재 좌석 점유자 정보 */
    private static class SeatOccupant {
        int memberId;
        String memberName;
        java.time.LocalDateTime startedAt;
    }

    /**
     * seat_id → 현재 사용 중인 회원 정보 매핑.
     * seat_usage 에서 ended_at IS NULL 인 내역만 모아 회원명과 함께 구성.
     */
    private java.util.Map<Integer, SeatOccupant> loadOccupantsBySeatId() {
        java.util.Map<Integer, SeatOccupant> map = new java.util.HashMap<>();
        try {
            com.tenco.seat_usage.SeatUsageDAO dao = new com.tenco.seat_usage.SeatUsageDAO();
            List<com.tenco.seat_usage.SeatUsageDTO> usages = dao.findAll();
            java.util.Map<Integer, String> nameById = new java.util.HashMap<>();
            for (MemberDTO m : memberService.getMemberList()) {
                nameById.put(m.getMemberId(), m.getName());
            }
            for (com.tenco.seat_usage.SeatUsageDTO u : usages) {
                if (u.getEndedAt() != null) continue;           // 진행 중만
                SeatOccupant o = new SeatOccupant();
                o.memberId   = u.getMemberId();
                o.memberName = nameById.getOrDefault(u.getMemberId(), "회원#" + u.getMemberId());
                o.startedAt  = u.getStartedAt();
                map.put(u.getSeatId(), o);
            }
        } catch (Exception ignored) {}
        return map;
    }

    /** 대시보드 상단 KPI 값들 */
    private static class DashboardStats {
        int totalSeat, inUseSeat, totalMember;
        long todayRevenue;
    }

    private DashboardStats loadDashboardStats() {
        DashboardStats s = new DashboardStats();
        try {
            List<SeatDTO> seats = seatService.getSeatList();
            s.totalSeat = seats.size();
            s.inUseSeat = (int) seats.stream().filter(x -> x.getStatus() == Status.IN_USE).count();
        } catch (Exception ignored) {}
        try {
            s.totalMember = memberService.getMemberList().size();
        } catch (Exception ignored) {}
        try {
            List<PaymentDTO> payments = paymentService.getPaymentList();
            if (payments != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                for (PaymentDTO p : payments) {
                    if ("SUCCESS".equalsIgnoreCase(p.getStatus())
                            && p.getPaidAt() != null
                            && p.getPaidAt().toLocalDate().equals(today)) {
                        s.todayRevenue += p.getAmount();
                    }
                }
            }
        } catch (Exception ignored) {}
        return s;
    }

    /** 결제 페이지 상단 통계 */
    private static class PaymentStats {
        long today, month;
        int total;
    }

    private PaymentStats loadPaymentStats() {
        PaymentStats s = new PaymentStats();
        try {
            List<PaymentDTO> list = paymentService.getPaymentList();
            if (list == null) return s;
            s.total = list.size();
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.YearMonth ym = java.time.YearMonth.now();
            for (PaymentDTO p : list) {
                if (!"SUCCESS".equalsIgnoreCase(p.getStatus())) continue;
                if (p.getPaidAt() == null) continue;
                if (p.getPaidAt().toLocalDate().equals(today)) s.today += p.getAmount();
                if (java.time.YearMonth.from(p.getPaidAt()).equals(ym)) s.month += p.getAmount();
            }
        } catch (Exception ignored) {}
        return s;
    }

    /** 대시보드 "최근 활동" 카드 행: {아이콘, 제목, 부가정보, 상대시간} */
    private String[][] loadRecentActivityRows() {
        try {
            // 1) 알림 테이블에서 전체 가져오기 (최신 순)
            List<MemberDTO> members = memberService.getMemberList();
            java.util.Map<Integer, String> memberNameById = new java.util.HashMap<>();
            for (MemberDTO m : members) memberNameById.put(m.getMemberId(), m.getName());

            List<NotificationDTO> all = new ArrayList<>();
            for (MemberDTO m : members) {
                List<NotificationDTO> ns = notificationService.getNotifications(m.getMemberId());
                if (ns != null) all.addAll(ns);
            }
            all.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            int limit = Math.min(5, all.size());
            String[][] rows = new String[limit][4];
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            for (int i = 0; i < limit; i++) {
                NotificationDTO n = all.get(i);
                String type = n.getType() == null ? "" : n.getType();
                String icon = switch (type) {
                    case "SEAT_START"   -> "🚪";
                    case "SEAT_END"     -> "🚶";
                    case "PAYMENT_DONE" -> "💳";
                    default             -> "🔔";
                };
                String name = memberNameById.getOrDefault(n.getMemberId(), "회원#" + n.getMemberId());
                String title = switch (type) {
                    case "SEAT_START"   -> name + " 입실";
                    case "SEAT_END"     -> name + " 퇴실";
                    case "PAYMENT_DONE" -> name + " 결제 완료";
                    default             -> name + " 알림";
                };
                String extra = n.getMessage() == null ? "—" : shortenMessage(n.getMessage());
                String when  = n.getCreatedAt() == null ? "-" : relativeTime(n.getCreatedAt(), now);

                rows[i][0] = icon;
                rows[i][1] = title;
                rows[i][2] = extra;
                rows[i][3] = when;
            }
            return rows;
        } catch (Exception e) {
            return new String[0][4];
        }
    }

    /** 알림 메시지에서 대표 키워드만 뽑아 간결하게 */
    private String shortenMessage(String msg) {
        if (msg == null) return "—";
        // 좌석번호 추출 (예: "A1 좌석") 우선
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([A-Za-z]?\\d+)\\s*좌석").matcher(msg);
        if (m.find()) return m.group(1);
        // 이용권명 추출 (예: "30시간권 결제")
        m = java.util.regex.Pattern.compile("(\\d+(?:시간|일)권)").matcher(msg);
        if (m.find()) return m.group(1);
        return msg.length() > 14 ? msg.substring(0, 14) + "…" : msg;
    }

    private String relativeTime(java.time.LocalDateTime past, java.time.LocalDateTime now) {
        long sec = java.time.Duration.between(past, now).getSeconds();
        if (sec < 0) sec = 0;
        if (sec < 60)       return sec + "초 전";
        if (sec < 3600)     return (sec / 60) + "분 전";
        if (sec < 86400)    return (sec / 3600) + "시간 전";
        if (sec < 86400L*7) return (sec / 86400) + "일 전";
        return past.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }

    /** 알림 테이블용 행: {아이콘, 제목, 본문, 시간, 미읽음여부} */
    private Object[][] loadNotificationRows() {
        try {
            List<MemberDTO> members = memberService.getMemberList();
            List<NotificationDTO> all = new ArrayList<>();
            for (MemberDTO m : members) {
                List<NotificationDTO> ns = notificationService.getNotifications(m.getMemberId());
                if (ns != null) all.addAll(ns);
            }
            all.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            Object[][] rows = new Object[all.size()][5];
            DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (int i = 0; i < all.size(); i++) {
                NotificationDTO n = all.get(i);
                String icon = switch (n.getType() == null ? "" : n.getType()) {
                    case "SEAT_START"   -> "🟢";
                    case "SEAT_END"     -> "🟡";
                    case "PAYMENT_DONE" -> "💳";
                    default             -> "🔔";
                };
                String title = switch (n.getType() == null ? "" : n.getType()) {
                    case "SEAT_START"   -> "입실 처리";
                    case "SEAT_END"     -> "퇴실 처리";
                    case "PAYMENT_DONE" -> "결제 완료";
                    default             -> "알림";
                };
                rows[i][0] = icon;
                rows[i][1] = title;
                rows[i][2] = n.getMessage() == null ? "" : n.getMessage();
                rows[i][3] = n.getCreatedAt() != null ? n.getCreatedAt().format(dfmt) : "";
                rows[i][4] = !n.isRead();
            }
            return rows;
        } catch (Exception e) {
            return new Object[0][5];
        }
    }

    /** 대시보드/결제 통계 라벨 재계산 */
    private void refreshDashboardStats() {
        try {
            DashboardStats s = loadDashboardStats();
            if (statTotalSeat    != null) statTotalSeat.setText(String.valueOf(s.totalSeat));
            if (statInUse        != null) statInUse.setText(String.valueOf(s.inUseSeat));
            if (statTotalMember  != null) statTotalMember.setText(String.valueOf(s.totalMember));
            if (statTodayRevenue != null) statTodayRevenue.setText(KRW.format(s.todayRevenue));
        } catch (Exception ignored) {}
    }

    private void refreshPaymentStats() {
        try {
            PaymentStats s = loadPaymentStats();
            if (statTodayRevenue   != null) statTodayRevenue.setText(KRW.format(s.today));
            if (statMonthRevenue   != null) statMonthRevenue.setText(KRW.format(s.month));
            if (statTotalPayments  != null) statTotalPayments.setText(String.valueOf(s.total));
        } catch (Exception ignored) {}
    }

    /** 좌석 변경 후 전역 갱신 */
    private void refreshSeatsEverywhere() {
        refreshSeatMap();
        refreshMiniSeatMap();
        refreshDashboardStats();
    }

    // ══════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════

    private void handleCheckIn() {
        String memberInput = JOptionPane.showInputDialog(this,
                "회원 ID 또는 전화번호 입력 (예: 1 또는 010-1111-1111):",
                "입실 처리", JOptionPane.PLAIN_MESSAGE);
        if (memberInput == null || memberInput.isBlank()) return;

        try {
            int memberId = memberService.resolveMemberId(memberInput);
            if (memberId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + memberInput + "' 에 해당하는 회원을 찾을 수 없습니다.\n" +
                        "회원 ID 또는 등록된 전화번호를 확인해주세요.",
                        "회원 조회 실패", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String seatInput = JOptionPane.showInputDialog(this,
                    "좌석 번호 또는 ID 입력 (예: A1, P3, 또는 1):",
                    "입실 처리", JOptionPane.PLAIN_MESSAGE);
            if (seatInput == null || seatInput.isBlank()) return;

            int seatId = seatService.resolveSeatId(seatInput);
            if (seatId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + seatInput + "' 에 해당하는 좌석을 찾을 수 없습니다.\n" +
                        "좌석관리 탭에서 등록된 좌석 번호(A1, P3 등)를 확인해주세요.",
                        "좌석 조회 실패", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean result = seatService.checkIn(memberId, seatId);

            if (result) {
                // 입실 완료 후 상세 정보 표시
                try {
                    SeatDTO seat = seatService.getSeatList().stream()
                            .filter(s -> s.getSeatId() == seatId)
                            .findFirst()
                            .orElse(null);
                    MemberDTO member = memberService.getMemberById(memberId);

                    String message = "✅ 입실 처리 완료!\n\n" +
                            "━━━━━━━━━━━━━━━━━━\n" +
                            "회원 ID: " + memberId + "\n" +
                            "회원명: " + (member != null ? member.getName() : "알 수 없음") + "\n" +
                            "전화번호: " + (member != null ? member.getPhone() : "알 수 없음") + "\n" +
                            "좌석 번호: " + (seat != null ? seat.getSeatNumber() : "알 수 없음") + "\n" +
                            "좌석 타입: " + (seat != null ? seat.getSeatType() : "알 수 없음") + "\n" +
                            "구역: " + (seat != null ? seat.getZone() : "알 수 없음") + "\n" +
                            "상태: IN_USE (사용 중)\n" +
                            "━━━━━━━━━━━━━━━━━━";

                    JOptionPane.showMessageDialog(this, message, "✅ 입실 완료", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "입실 처리 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                }
                refreshSeatsEverywhere();
                refreshMemberTable();
            } else {
                JOptionPane.showMessageDialog(this, "입실 처리 실패", "실패", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckOut() {
        String memberInput = JOptionPane.showInputDialog(this,
                "회원 ID 또는 전화번호 입력 (예: 1 또는 010-1111-1111):",
                "퇴실 처리", JOptionPane.PLAIN_MESSAGE);
        if (memberInput == null || memberInput.isBlank()) return;

        try {
            int memberId = memberService.resolveMemberId(memberInput);
            if (memberId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + memberInput + "' 에 해당하는 회원을 찾을 수 없습니다.\n" +
                        "회원 ID 또는 등록된 전화번호를 확인해주세요.",
                        "회원 조회 실패", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean result = seatService.checkOut(memberId);

            if (result) {
                try {
                    MemberDTO member = memberService.getMemberById(memberId);
                    String message = "✅ 퇴실 처리 완료!\n\n" +
                            "━━━━━━━━━━━━━━━━━━\n" +
                            "회원 ID: " + memberId + "\n" +
                            "회원명: " + (member != null ? member.getName() : "알 수 없음") + "\n" +
                            "전화번호: " + (member != null ? member.getPhone() : "알 수 없음") + "\n" +
                            "━━━━━━━━━━━━━━━━━━";
                    JOptionPane.showMessageDialog(this, message, "✅ 퇴실 완료", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "퇴실 처리 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                }
                refreshSeatsEverywhere();
                refreshMemberTable();
            } else {
                JOptionPane.showMessageDialog(this, "퇴실 처리 실패", "실패", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterMemberDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("이름:"));
        panel.add(nameField);
        panel.add(new JLabel("전화번호:"));
        panel.add(phoneField);
        panel.add(new JLabel("이메일:"));
        panel.add(emailField);
        panel.add(new JLabel("비밀번호:"));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(this, panel, "회원 등록", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                MemberDTO member = MemberDTO.builder()
                        .name(nameField.getText())
                        .phone(phoneField.getText())
                        .email(emailField.getText())
                        .password(new String(passwordField.getPassword()))
                        .build();

                boolean isSuccess = memberService.registerMember(member);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "회원 등록 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshMemberTable();
                    refreshDashboardStats();
                } else {
                    JOptionPane.showMessageDialog(this, "이미 사용 중인 정보입니다", "실패", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditMemberDialog() {
        String idStr = JOptionPane.showInputDialog(this, "수정할 회원 ID 입력:", "회원 수정", JOptionPane.PLAIN_MESSAGE);
        if (idStr == null || idStr.isBlank()) return;

        try {
            int memberId = Integer.parseInt(idStr);
            MemberDTO member = memberService.getMemberById(memberId);

            JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JTextField nameField = new JTextField(member.getName());
            JTextField phoneField = new JTextField(member.getPhone());
            JTextField emailField = new JTextField(member.getEmail());
            JPasswordField passwordField = new JPasswordField(member.getPassword());

            panel.add(new JLabel("이름:"));
            panel.add(nameField);
            panel.add(new JLabel("전화번호:"));
            panel.add(phoneField);
            panel.add(new JLabel("이메일:"));
            panel.add(emailField);
            panel.add(new JLabel("비밀번호:"));
            panel.add(passwordField);
            panel.add(new JLabel(""));
            panel.add(new JLabel(""));

            int result = JOptionPane.showConfirmDialog(this, panel, "회원 수정", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                MemberDTO updatedMember = MemberDTO.builder()
                        .memberId(memberId)
                        .name(nameField.getText())
                        .phone(phoneField.getText())
                        .email(emailField.getText())
                        .password(new String(passwordField.getPassword()))
                        .build();

                boolean isSuccess = memberService.modifyMember(updatedMember);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "회원 수정 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshMemberTable();
                } else {
                    JOptionPane.showMessageDialog(this, "회원 수정 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteMemberDialog() {
        String idStr = JOptionPane.showInputDialog(this, "삭제할 회원 ID 입력:", "회원 삭제", JOptionPane.PLAIN_MESSAGE);
        if (idStr == null || idStr.isBlank()) return;

        try {
            int memberId = Integer.parseInt(idStr);
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean result = memberService.removeMember(memberId);
                if (result) {
                    JOptionPane.showMessageDialog(this, "회원 삭제 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshMemberTable();
                    refreshDashboardStats();
                } else {
                    JOptionPane.showMessageDialog(this, "회원 삭제 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddSeatDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField seatNumberField = new JTextField();
        String[] types = {"STANDARD", "PREMIUM"};
        JComboBox<String> typeCombo = new JComboBox<>(types);

        panel.add(new JLabel("좌석 번호:"));
        panel.add(seatNumberField);
        panel.add(new JLabel("좌석 타입:"));
        panel.add(typeCombo);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(this, panel, "좌석 추가", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                SeatDTO seat = SeatDTO.builder()
                        .seatNumber(seatNumberField.getText())
                        .seatType(SeatType.valueOf((String) typeCombo.getSelectedItem()))
                        .status(Status.AVAILABLE)
                        .build();

                boolean isSuccess = seatService.addSeat(seat);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "좌석 추가 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshSeatsEverywhere();
                } else {
                    JOptionPane.showMessageDialog(this, "좌석 추가 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangeSeatStatusDialog() {
        String seatInput = JOptionPane.showInputDialog(this,
                "좌석 번호 또는 ID 입력 (예: A1, P3, 또는 1):",
                "좌석 상태 변경", JOptionPane.PLAIN_MESSAGE);
        if (seatInput == null || seatInput.isBlank()) return;

        try {
            int seatId = seatService.resolveSeatId(seatInput);
            if (seatId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + seatInput + "' 에 해당하는 좌석을 찾을 수 없습니다.",
                        "좌석 조회 실패", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String[] statuses = {"AVAILABLE", "IN_USE", "DISABLED"};
            String selectedStatus = (String) JOptionPane.showInputDialog(this, "변경할 상태 선택:", "좌석 상태 변경",
                    JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[0]);

            if (selectedStatus != null) {
                boolean result = seatService.modifySeatStatus(seatId, Status.valueOf(selectedStatus));
                if (result) {
                    JOptionPane.showMessageDialog(this, "좌석 상태 변경 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshSeatsEverywhere();
                } else {
                    JOptionPane.showMessageDialog(this, "좌석 상태 변경 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteSeatDialog() {
        String seatInput = JOptionPane.showInputDialog(this,
                "삭제할 좌석 번호 또는 ID 입력 (예: A1, P3, 또는 1):",
                "좌석 삭제", JOptionPane.PLAIN_MESSAGE);
        if (seatInput == null || seatInput.isBlank()) return;

        try {
            int seatId = seatService.resolveSeatId(seatInput);
            if (seatId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "'" + seatInput + "' 에 해당하는 좌석을 찾을 수 없습니다.",
                        "좌석 조회 실패", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean result = seatService.removeSeat(seatId);
                if (result) {
                    JOptionPane.showMessageDialog(this, "좌석 삭제 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshSeatsEverywhere();
                } else {
                    JOptionPane.showMessageDialog(this, "좌석 삭제 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddTicketDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        String[] types = {"TIME", "PERIOD"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        JTextField durationField = new JTextField();
        JTextField priceField = new JTextField();

        panel.add(new JLabel("이용권명:"));
        panel.add(nameField);
        panel.add(new JLabel("타입:"));
        panel.add(typeCombo);
        panel.add(new JLabel("기간/시간:"));
        panel.add(durationField);
        panel.add(new JLabel("가격:"));
        panel.add(priceField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(this, panel, "이용권 추가", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                TicketDTO ticket = TicketDTO.builder()
                        .name(nameField.getText())
                        .type(TicketType.valueOf((String) typeCombo.getSelectedItem()))
                        .durationValue(Integer.parseInt(durationField.getText()))
                        .price(Integer.parseInt(priceField.getText()))
                        .build();

                boolean isSuccess = ticketService.addTicket(ticket);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "이용권 추가 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshTicketTable();
                } else {
                    JOptionPane.showMessageDialog(this, "이용권 추가 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDeleteTicketDialog() {
        String ticketIdStr = JOptionPane.showInputDialog(this, "삭제할 이용권 ID 입력:", "이용권 삭제", JOptionPane.PLAIN_MESSAGE);
        if (ticketIdStr == null || ticketIdStr.isBlank()) return;

        try {
            int ticketId = Integer.parseInt(ticketIdStr);
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean result = ticketService.removeTicket(ticketId);
                if (result) {
                    JOptionPane.showMessageDialog(this, "이용권 삭제 완료!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    refreshTicketTable();
                } else {
                    JOptionPane.showMessageDialog(this, "이용권 삭제 실패", "실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarkAllAsRead() {
        JOptionPane.showMessageDialog(this, "모든 알림을 읽음 처리했습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        // TODO: 모든 알림을 읽음 처리하는 로직 추가 필요
    }

    private void handleDeleteAllNotifications() {
        int confirm = JOptionPane.showConfirmDialog(this, "모든 알림을 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "모든 알림을 삭제했습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            // TODO: 모든 알림을 삭제하는 로직 추가 필요
        }
    }
}
